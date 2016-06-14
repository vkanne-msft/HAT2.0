package hatdex.hat.api.service

import akka.actor.{ ActorRefFactory, ActorContext }
import akka.event.{ Logging, LoggingAdapter }
import hatdex.hat.api.DatabaseInfo
import hatdex.hat.api.models._
import hatdex.hat.api.models.stats.{ DataCreditStats, DataDebitStats, DataTableStats, DataFieldStats }

import scala.collection.immutable.HashMap

//import hatdex.hat.dal.SlickPostgresDriver.simple._
import hatdex.hat.dal.SlickPostgresDriver.api._
import hatdex.hat.dal.Tables._
import org.joda.time.LocalDateTime
import hatdex.hat.authentication.models.User
import hatdex.hat.Utils

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{ Failure, Success }

trait StatsService {

  val logger: LoggingAdapter
  def actorRefFactory: ActorRefFactory

  val statsActor = actorRefFactory.actorSelection("/user/stats-service-supervisor/hatdex.marketplace.stats-service")

  def recordDataDebitOperation(
    dd: ApiDataDebit,
    user: User,
    operationType: DataDebitOperations.DataDebitOperation,
    logEntry: String): Future[Int] = {
    logger.debug(s"Record Data Debit operation: ${dd.key}, ${operationType.toString} by ${user.name}")

    val ddOperation = StatsDataDebitOperationRow(0, LocalDateTime.now(), dd.key.get, user.userId, operationType.toString)
    val ddUser = user.copy(pass = None)

    DatabaseInfo.db.run {
      (StatsDataDebitOperation returning StatsDataDebitOperation.map(_.recordId)) += ddOperation
    } map {
      case recordId =>
        val stats = DataDebitStats("datadebit", dd, operationType.toString, ddOperation.dateCreated, ddUser, None, logEntry)
        logger.debug(s"Data Debit operation recorded, sending stats to actor $stats")
        statsActor ! stats
        recordId
    } recover {
      case e =>
        logger.error(s"Error while saving data debit statistics: ${e.getMessage}")
        throw e
    }
  }

  def recordDataDebitRetrieval(
    dd: ApiDataDebit,
    values: Seq[ApiEntity],
    user: User,
    logEntry: String): Future[Unit] = {
    val ddOperation = StatsDataDebitOperationRow(0, LocalDateTime.now(), dd.key.get, user.userId, DataDebitOperations.GetValues().toString)
    val ddUser = user.copy(pass = None)
    val fOperation = DatabaseInfo.db.run {
      (StatsDataDebitOperation returning StatsDataDebitOperation) += ddOperation
    } andThen {
      case _ =>
        statsActor ! DataDebitStats("datadebit", dd, ddOperation.operation, ddOperation.dateCreated, ddUser, None, logEntry)
    }
    fOperation.map { o =>
      ()
    }
  }

  def recordDataDebitRetrieval(
    dd: ApiDataDebit,
    values: ApiDataDebitOut,
    user: User,
    logEntry: String): Future[Unit] = {
    val ddOperation = StatsDataDebitOperationRow(0, LocalDateTime.now(), dd.key.get, user.userId, DataDebitOperations.GetValues().toString)
    val ddUser = user.copy(pass = None)
    values.bundleContextless.map { bundle =>
      val (totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats) = getBundleStats(bundle)
      storeBundleStats(ddOperation, totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats)
        .andThen {
          case _ =>
            val stats = convertBundleStats(tableValueStats, fieldValueStats)
            statsActor ! DataDebitStats("datadebit", dd, ddOperation.operation, ddOperation.dateCreated, ddUser, Some(stats), logEntry)
        }
    } getOrElse {
      // Not contextless bundle info, do nothing
      Future {}
    }
  }

  def recordDataInbound(records: Seq[ApiRecordValues], user: User, logEntry: String): Future[Unit] = {
    val userInfo = user.copy(pass = None)
    val allFields = records.flatMap(_.values.flatMap(_.field))
    getFieldsetStats(allFields) map { stats =>
      statsActor ! DataCreditStats("datacredit", "Data Record Inbound", LocalDateTime.now(), userInfo, Some(stats), logEntry)
    }
  }

  def recordDataValuesInbound(values: Seq[ApiDataValue], user: User, logEntry: String): Future[Unit] = {
    val userInfo = user.copy(pass = None)
    val allFields = values.flatMap(_.field)
    getFieldsetStats(allFields) map { stats =>
      statsActor ! DataCreditStats("datacredit", "Data Values Inbound", LocalDateTime.now(), userInfo, Some(stats), logEntry)
    }
  }

  private def getFieldsetStats(fields: Seq[ApiDataField]): Future[Seq[DataTableStats]] = {
    val fieldCounts = fields.groupBy(f => f.copy(id = f.id, tableId = f.tableId, name = f.name, values = None))
      .mapValues(_.size)

    val tablesOfInterest = fieldCounts.keySet.map(_.tableId.getOrElse(0))
    DatabaseInfo.db.run {
      // Find tables of interest
      DataTable.filter(_.id inSet tablesOfInterest).result
    } map { tablesFetched =>
      tablesFetched map { dataTable =>
        // For each table, take the fields that are part of the table
        val tableFields = fieldCounts.filter(_._1.tableId.contains(dataTable.id))
          .map {
            case (field, count) =>
              // Construct field stats
              DataFieldStats(field.name, dataTable.name, dataTable.sourceName, count)
          }
          .toSeq
        // Construct table stats from a sequence of field stats
        DataTableStats(dataTable.name, dataTable.sourceName, tableFields, None, tableFields.map(_.valueCount).sum)
      }
    }
  }

  def convertBundleStats(tableValueStats: HashMap[ApiDataTable, Int], fieldValueStats: HashMap[ApiDataField, Int]): Seq[DataTableStats] = {
    val stats = tableValueStats.map {
      case (table, values) =>
        val matchingFields = fieldValueStats.filter(_._1.tableId == table.id)
          .map {
            case (field, fieldValues) =>
              DataFieldStats(field.name, table.name, table.source, fieldValues)
          }

        DataTableStats(table.name, table.source, matchingFields.toSeq, None, values)
    }
    stats.toSeq
  }

  def getBundleStats(bundle: ApiBundleContextlessData): (Int, Map[ApiBundleTable, Int], HashMap[ApiDataTable, Int], HashMap[ApiDataField, Int]) = {
    val groupStats = bundle.dataGroups.flatMap { group => // Only one group until Joins come in
      group.map {
        case (groupName: String, bundleTable: ApiBundleTable) =>
          val bundleTableRecords = getBundleTableRecordCount(bundleTable)
          val aggregateTableValueCounts = getTableValueCounts(bundleTable)
          val aggregateFieldValueCounts = getFieldValueCounts(bundleTable)

          (bundleTableRecords, aggregateTableValueCounts, aggregateFieldValueCounts)
      }
    }
    val bundleTableRecordCounts = groupStats.map(_._1)
    val tableValueCounts = groupStats.map(_._2)
    val fieldValueCounts = groupStats.map(_._3)

    val bundleTableStats = bundleTableRecordCounts.groupBy(_._1).map {
      case (bundleTable, aggBundleTableCounts) =>
        bundleTable -> aggBundleTableCounts.map(_._2).sum
    }

    val tableValueStats = Utils.mergeMap(tableValueCounts)((v1, v2) => v1 + v2)
    val fieldValueStats = Utils.mergeMap(fieldValueCounts)((v1, v2) => v1 + v2)

    val totalBundleRecords = bundleTableStats.values.sum

    (totalBundleRecords, bundleTableStats, tableValueStats, fieldValueStats)
  }

  def storeBundleStats(
    ddOperation: StatsDataDebitOperationRow,
    totalBundleRecords: Int,
    bundleTableStats: Map[ApiBundleTable, Int],
    tableValueStats: HashMap[ApiDataTable, Int],
    fieldValueStats: HashMap[ApiDataField, Int]): Future[Unit] = {

    val fOperation = DatabaseInfo.db.run {
      (StatsDataDebitOperation returning StatsDataDebitOperation) += ddOperation
    }
    val fStatements = fOperation.map { o =>
      val dbTableStatsInsert = StatsDataDebitDataTableAccess ++=
        tableValueStats map {
          case (table, valueCount) =>
            StatsDataDebitDataTableAccessRow(0, o.dateCreated, table.id.get, o.recordId, valueCount)
        }

      val dbFieldStatsInsert = StatsDataDebitDataFieldAccess ++=
        fieldValueStats map {
          case (field, valueCount) =>
            StatsDataDebitDataFieldAccessRow(0, o.dateCreated, field.id.get, o.recordId, valueCount)
        }

      val ddRecordCountInsert = StatsDataDebitRecordCount +=
        StatsDataDebitRecordCountRow(0, o.dateCreated, o.recordId, totalBundleRecords)

      val dbBundleTableStatsInsert = StatsDataDebitClessBundleRecords ++=
        bundleTableStats map {
          case (bundleTable, recordCount) =>
            StatsDataDebitClessBundleRecordsRow(0, o.dateCreated, bundleTable.id.get, o.recordId, recordCount)
        }

      DBIO.seq(
        dbTableStatsInsert,
        dbFieldStatsInsert,
        dbBundleTableStatsInsert,
        ddRecordCountInsert)
    }

    fStatements.flatMap { statements =>
      DatabaseInfo.db.run(statements)
    }
  }

  def getBundleTableRecordCount(bundleTable: ApiBundleTable) = {
    (bundleTable, bundleTable.data.map(data => data.length).getOrElse(0))
  }

  def getTableValueCounts(bundleTable: ApiBundleTable): HashMap[ApiDataTable, Int] = {
    // Each group consists of a sequence of data records
    val tableValueCounts = bundleTable.data.getOrElse(Seq()).flatMap { record: ApiDataRecord =>
      record.tables.getOrElse(Seq()).map(countTableValues)
    }
    Utils.mergeMap(tableValueCounts)((v1, v2) => v1 + v2)
  }

  def getFieldValueCounts(bundleTable: ApiBundleTable): HashMap[ApiDataField, Int] = {
    // We also want to know what Data Attributes were retrieved, and how many items each of them contained
    val fieldValueCounts = bundleTable.data.getOrElse(Seq()).flatMap { record: ApiDataRecord =>
      record.tables.getOrElse(Seq()).map(countFieldValues)
    }
    Utils.mergeMap(fieldValueCounts)((v1, v2) => v1 + v2)
  }

  private def countTableValues(dataTable: ApiDataTable): HashMap[ApiDataTable, Int] = {
    val valueCount = countFieldValues(dataTable).values.sum
    val tableValueCount = HashMap(
      dataTable.copy(fields = None, subTables = None) -> valueCount)

    val counts = dataTable.subTables.getOrElse(Seq()).map(countTableValues)
    Utils.mergeMap(counts :+ tableValueCount)((v1, v2) => v1 + v2)
  }

  private def countFieldValues(dataTable: ApiDataTable): HashMap[ApiDataField, Int] = {
    val fieldCounts = HashMap(dataTable.fields.getOrElse(Seq()) map { field => // For all fields
      val fieldValues = field.values.map(_.length).getOrElse(0) // Count the number of values or 0 if no values
      val fieldUniform = field.copy(values = None)
      fieldUniform -> fieldValues
    }: _*)
    val counts = dataTable.subTables.getOrElse(Seq()).map(countFieldValues)
    Utils.mergeMap(counts :+ fieldCounts)((v1, v2) => v1 + v2)
  }
}

object DataDebitOperations {
  sealed trait DataDebitOperation
  case class Create() extends DataDebitOperation
  case class Enable() extends DataDebitOperation
  case class Disable() extends DataDebitOperation
  case class GetValues() extends DataDebitOperation
}
