package hatdex.hat.api.endpoints

import akka.event.{Logging, LoggingAdapter}
import hatdex.hat.api.TestDataCleanup
import hatdex.hat.api.endpoints.jsonExamples.DataExamples
import hatdex.hat.api.json.JsonProtocol
import hatdex.hat.api.models._
import hatdex.hat.authentication.HatAuthTestHandler
import hatdex.hat.authentication.authenticators.{ AccessTokenHandler, UserPassHandler }
import org.specs2.mutable.Specification
import org.specs2.specification.BeforeAfterAll
import spray.http.HttpHeaders.RawHeader
import spray.http.HttpMethods._
import spray.http.StatusCodes._
import spray.http._
import spray.httpx.SprayJsonSupport._
import spray.json._
import spray.testkit.Specs2RouteTest

import scala.concurrent.duration._

class DataSpec extends Specification with Specs2RouteTest with Data with BeforeAfterAll {
  def actorRefFactory = system

  val logger: LoggingAdapter = Logging.getLogger(system, "API-Access")

  override def accessTokenHandler = AccessTokenHandler.AccessTokenAuthenticator(authenticator = HatAuthTestHandler.AccessTokenHandler.authenticator).apply()

  override def userPassHandler = UserPassHandler.UserPassAuthenticator(authenticator = HatAuthTestHandler.UserPassHandler.authenticator).apply()

  implicit val routeTestTimeout = RouteTestTimeout(5.second)

  import JsonProtocol._

  def beforeAll() = {}

  // Clean up all data
  def afterAll() = {
    db.withSession { implicit session =>
      TestDataCleanup.cleanupAll
      session.close()
    }
  }

  sequential

  val ownerAuthToken = HatAuthTestHandler.validUsers.find(_.role == "owner").map(_.userId).flatMap { ownerId =>
    HatAuthTestHandler.validAccessTokens.find(_.userId == ownerId).map(_.accessToken)
  } getOrElse ("")
  val ownerAuthHeader = RawHeader("X-Auth-Token", ownerAuthToken)

  def createBasicTables = {
    val dataTable = HttpRequest(POST, "/data/table")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchen)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchen")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

    val dataSubtable = HttpRequest(POST, "/data/table")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.tableKitchenElectricity)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[String] must contain("kitchenElectricity")
        responseAs[String] must contain("fibaro")
        responseAs[ApiDataTable]
      }

    (dataTable, dataSubtable)
  }

  def populateDataReusable = {
    // Create main table
    val dataTable = HttpRequest(GET, "/data/table?name=kitchen&source=fibaro")
      .withHeaders(ownerAuthHeader) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo OK
        responseAs[ApiDataTable]
      }

    dataTable.id must beSome

    // Create sub-table
    val dataSubtable = HttpRequest(GET, "/data/table?name=kitchenElectricity&source=fibaro")
      .withHeaders(ownerAuthHeader) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo OK
        responseAs[ApiDataTable]
      }

    dataSubtable.id must beSome

    // Link table with subtable
    HttpRequest(POST, s"/data/table/${dataTable.id.get}/table/${dataSubtable.id.get}")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo OK
      }

    // Create fields
    val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
    val completeTableField = field.copy(tableId = dataTable.id)

    val dataField = HttpRequest(POST, "/data/field")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, completeTableField.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
    dataField.id must beSome

    val completeSubTableField1 = field.copy(tableId = dataSubtable.id, name = "subtableTestField1")
    val dataSubfield1 = HttpRequest(POST, "/data/field")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, completeSubTableField1.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
    dataSubfield1.id must beSome

    val completeSubTableField2 = field.copy(tableId = dataSubtable.id, name = "subtableTestField2")
    val dataSubfield2 = HttpRequest(POST, "/data/field")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, completeSubTableField2.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[ApiDataField]
      }
    dataSubfield2.id must beSome

    // Create Data Record
    val record = HttpRequest(POST, "/data/record")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.testRecord)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[ApiDataRecord]
      }
    record.id must beSome

    // Batch-fill it with data
    val dataValues = Seq(
      new ApiDataValue(None, None, None, "testValue1", Some(dataField), None),
      new ApiDataValue(None, None, None, "testValue2", Some(dataSubfield1), None),
      new ApiDataValue(None, None, None, "testValue3", Some(dataSubfield2), None))

    HttpRequest(POST, s"/data/record/${record.id.get}/values")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, dataValues.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
      }

    // Create another record
    val record2 = HttpRequest(POST, "/data/record")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.testRecord2)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        responseAs[ApiDataRecord]
      }
    record2.id must beSome

    // Fill it with data one-by-one
    val dataValues2 = Seq(
      new ApiDataValue(None, None, None, "testValue2-1", Some(dataField), Some(record2)),
      new ApiDataValue(None, None, None, "testValue2-2", Some(dataSubfield1), Some(record2)),
      new ApiDataValue(None, None, None, "testValue2-3", Some(dataSubfield2), Some(record2)))

    dataValues2 map { dataValue =>
      HttpRequest(POST, "/data/value")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, dataValue.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }
    }

    // Create one more record for batch-inserting
    val recordValues = ApiRecordValues(
      ApiDataRecord(None, None, None, "testRecord 4", None),
      Seq(
        new ApiDataValue(None, None, None, "testValue4-1", Some(dataField), None),
        new ApiDataValue(None, None, None, "testValue4-2", Some(dataSubfield1), None),
        new ApiDataValue(None, None, None, "testValue4-3", Some(dataSubfield2), None)))

    // Create another record
    HttpRequest(POST, "/data/record/values")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, recordValues.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        val resp = responseAs[String]
        resp must contain("testValue4-1")
        resp must contain("testValue4-2")
        resp must contain("testValue4-3")
        resp must not contain ("testValue2-1")
        responseAs[ApiRecordValues]
      }

    // Create one more record for batch-inserting
    val recordValueList = Seq(
      ApiRecordValues(
        ApiDataRecord(None, None, None, "testRecord 5", None),
        Seq(
          new ApiDataValue(None, None, None, "testValue5-1", Some(dataField), None),
          new ApiDataValue(None, None, None, "testValue5-2", Some(dataSubfield1), None),
          new ApiDataValue(None, None, None, "testValue5-3", Some(dataSubfield2), None))),
      ApiRecordValues(
        ApiDataRecord(None, None, None, "testRecord 6", None),
        Seq(
          new ApiDataValue(None, None, None, "testValue6-1", Some(dataField), None),
          new ApiDataValue(None, None, None, "testValue6-2", Some(dataSubfield1), None),
          new ApiDataValue(None, None, None, "testValue6-3", Some(dataSubfield2), None))))

    HttpRequest(POST, "/data/record/values")
      .withHeaders(ownerAuthHeader)
      .withEntity(HttpEntity(MediaTypes.`application/json`, recordValueList.toJson.toString)) ~>
      sealRoute(routes) ~>
      check {
        response.status should be equalTo Created
        val resp = responseAs[String]
        resp must contain("testValue5-1")
        resp must contain("testValue5-2")
        resp must contain("testValue5-3")
        resp must contain("testValue6-1")
        resp must contain("testValue6-2")
        resp must contain("testValue6-3")
        resp must not contain ("testValue2-1")
        responseAs[Seq[ApiRecordValues]]
      }

    (dataTable, dataField, record)
  }

  "DataService" should {
    "Accept new tables created" in {
      createBasicTables match {
        case (dataTable, dataSubtable) =>
          dataTable.id must beSome
          dataSubtable.id must beSome

          HttpRequest(POST, s"/data/table/${dataTable.id.get}/table/${dataSubtable.id.get}")
            .withHeaders(ownerAuthHeader)
            .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
            sealRoute(routes) ~> check {
              response.status should be equalTo OK
            }

          HttpRequest(GET, s"/data/table/${dataTable.id.get}")
            .withHeaders(ownerAuthHeader) ~>
            sealRoute(routes) ~> check {
              response.status should be equalTo OK
              responseAs[String] must contain("kitchen")
              responseAs[String] must contain("subTables")
              responseAs[String] must contain("kitchenElectricity")
            }
      }
    }

    "Accept new nested tables" in {
      val dataTable = HttpRequest(POST, "/data/table")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.nestedTableKitchen)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
          val responseString = responseAs[String]
          responseString must contain("largeKitchen")
          responseString must contain("fibaro")
          responseString must contain("largeKitchenElectricity")
          responseString must contain("tableTestField4")
          responseAs[ApiDataTable]
        }
      dataTable.id must beSome

      HttpRequest(GET, s"/data/table/${dataTable.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          val responseString = responseAs[String]
          responseString must contain("largeKitchen")
          responseString must contain("fibaro")
          responseString must contain("largeKitchenElectricity")
          responseString must contain("tableTestField4")
        }
    }

    "Reject incorrect table linking" in {
      HttpRequest(POST, s"/data/table/0/table/1")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.relationshipParent)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }
    }

    "Allow table fields to be created" in {
      val dataTable = HttpRequest(GET, "/data/table?name=kitchenElectricity&source=fibaro&")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          responseAs[String] must contain("kitchenElectricity")
          responseAs[String] must contain("fibaro")
          responseAs[ApiDataTable]
        }

      dataTable.id must beSome

      val field = JsonParser(DataExamples.testField).convertTo[ApiDataField]
      val completeField = field.copy(tableId = dataTable.id)

      HttpRequest(POST, "/data/field")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, completeField.toJson.toString)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo Created
        }

      HttpRequest(GET, s"/data/table/${dataTable.id.get}")
        .withHeaders(ownerAuthHeader) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo OK
          responseAs[String] must contain("kitchen")
          responseAs[String] must contain("fields")
          responseAs[String] must contain("tableTestField")
        }
    }

    "Reject fields to non-existing tables" in {
      HttpRequest(POST, "/data/field")
        .withHeaders(ownerAuthHeader)
        .withEntity(HttpEntity(MediaTypes.`application/json`, DataExamples.testField)) ~>
        sealRoute(routes) ~>
        check {
          response.status should be equalTo BadRequest
        }
    }

    "Accept and provide data in the right formats" in {
      populateDataReusable match {
        case (dataTable, dataField, record) =>
          // Make sure that the right data elements are contained in the different kinds of responses
          HttpRequest(GET, s"/data/record/${record.id.get}/values")
            .withHeaders(ownerAuthHeader) ~>
            sealRoute(routes) ~>
            check {
              response.status should be equalTo OK
              responseAs[String] must contain("testValue1")
              responseAs[String] must contain("testValue2")
              responseAs[String] must contain("testValue3")
              responseAs[String] must not contain ("testValue2-1")
              responseAs[String] must contain("testRecord 1")
            }

          HttpRequest(GET, s"/data/field/${dataField.id.get}/values")
            .withHeaders(ownerAuthHeader) ~>
            sealRoute(routes) ~>
            check {
              response.status should be equalTo OK
              responseAs[String] must contain("testValue1")
              responseAs[String] must contain("testValue2-1")
              responseAs[String] must not contain ("testValue3")
            }

          HttpRequest(GET, s"/data/table/${dataTable.id.get}/values")
            .withHeaders(ownerAuthHeader) ~>
            sealRoute(routes) ~>
            check {
              response.status should be equalTo OK
              responseAs[String] must contain("testValue1")
              responseAs[String] must contain("testValue2")
              responseAs[String] must contain("testValue3")
              responseAs[String] must contain("testValue2-1")
              responseAs[String] must contain("testValue2-2")
              responseAs[String] must contain("testValue2-3")
              responseAs[String] must contain("testRecord 1")
              responseAs[String] must contain("testRecord 2")
            }
      }
    }
  }
}

