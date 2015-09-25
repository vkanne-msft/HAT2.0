import dal.Tables._
import org.joda.time.LocalDateTime
import dal.SlickPostgresDriver.simple._

object TestFixtures {
  def prepareContextualStructures(implicit sesion: Session) = {
    // Data tables
    val dataTablesRows = Seq(
      new DataTableRow(1, LocalDateTime.now(), LocalDateTime.now(), "Facebook", "facebook"),
      new DataTableRow(2, LocalDateTime.now(), LocalDateTime.now(), "events", "facebook"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "me", "facebook"),
      new DataTableRow(3, LocalDateTime.now(), LocalDateTime.now(), "Fibaro", "Fibaro"),
      new DataTableRow(4, LocalDateTime.now(), LocalDateTime.now(), "Fitbit", "Fitbit"),
      new DataTableRow(5, LocalDateTime.now(), LocalDateTime.now(), "locations", "facebook"))

    DataTables.forceInsertAll(dataTablesRows: _*)


    /*val findFacebookTableId = dataTables.filter(_.name === "Facebook").map(_.id).run.head
    val findEventsTableId = dataTables.filter(_.name === "events").map(_.id).run.head
    val findMeTableId = dataTables.filter(_.name === "me").map(_.id).run.head
    val findlocationsTableId = dataTables.filter(_.name === "locations").map(_.id).run.head
    val findFibaroTableId = dataTables.filter(_.name === "Fibaro").map(_.id).run.head
    val findFitbitTableId = dataTables.filter(_.name === "Fitbit").map(_.id).run.head
    */
    val dataTableToTableCrossRefRows = Seq(
      new DataTabletotablecrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", findFacebookTableId, findEventsTableId),
      new DataTabletotablecrossrefRow(2, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", findFacebookTableId, findlocationsTableId),
      new DataTabletotablecrossrefRow(3, LocalDateTime.now(), LocalDateTime.now(), "Parent_Child", findFacebookTableId, findMeTableId))

    DataTabletotablecrossref.forceInsertAll(dataTableToTableCrossRefRows: _*)

    val dataFieldRows = Seq(
      new DataFieldRow(1, LocalDateTime.now(), LocalDateTime.now(), "weight", findMeTableId),
      new DataFieldRow(2, LocalDateTime.now(), LocalDateTime.now(), "elevation", findlocationsTableId),
      new DataFieldRow(3, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", findEventsTableId),
      new DataFieldRow(4, LocalDateTime.now(), LocalDateTime.now(), "size", findFibaroTableId),
      new DataFieldRow(5, LocalDateTime.now(), LocalDateTime.now(), "temperature", findFibaroTableId))

    DataField.forceInsertAll(dataFieldRows: _*)


    val dataRecordRows = Seq(
      new DataRecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "FacebookMe"),
      new DataRecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "FacebookLocation"),
      new DataRecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "FibaroKitchen"),
      new DataRecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "FacebookEvent"),
      new DataRecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "FibaroBathroom"))

    DataRecord.forceInsertAll(dataRecordRows: _*)


    /*val findFacebookMeRecordId = dataTables.filter(_.name === "FacebookMe").map(_.id).run.head
    val findFacebookLocationRecordId = dataTables.filter(_.name === "FacebookLocation").map(_.id).run.head
    val findFibaroKitchenRecordId = dataTables.filter(_.name === "FibaroKitchen").map(_.id).run.head
    val findFacebookEventRecordId = dataTables.filter(_.name === "FacebookEvent").map(_.id).run.head
    val findFibaroBathroomRecordId = dataTables.filter(_.name === "FibaroBathroom").map(_.id).run.head

    val findWeightFieldId = dataTables.filter(_.name === "weight").map(_.id).run.head
    val findElevationFieldId = dataTables.filter(_.name === "elevation").map(_.id).run.head
    val findKichenElectricityFieldId = dataTables.filter(_.name === "kichenElectricity").map(_.id).run.head
    val findSizeFieldId = dataTables.filter(_.name === "size").map(_.id).run.head
    val findTemperatureFieldId = dataTables.filter(_.name === "temperature").map(_.id).run.head
    */
    val dataValueRows = Seq(
      new DataValueRow(1, LocalDateTime.now(), LocalDateTime.now(), "62", findWeightFieldId, findFacebookMeRecordId),
      new DataValueRow(2, LocalDateTime.now(), LocalDateTime.now(), "300", findElevationFieldId, findFacebookLocationRecordId),
      new DataValueRow(3, LocalDateTime.now(), LocalDateTime.now(), "20KwH", findKichenElectricityFieldId, findFibaroKitchenRecordId),
      new DataValueRow(4, LocalDateTime.now(), LocalDateTime.now(), "Having a Shower", findFacebookEventRecordId, findFibaroBathroomRecordId),
      new DataValueRow(5, LocalDateTime.now(), LocalDateTime.now(), "25", findTemperatureFieldId, findFacebookEventRecordId))

    DataValue.forceInsertAll(dataValueRows: _*)

    // contextualisation tools 
    val systemUnitOfMeasurementRows = Seq(
      new SystemUnitofmeasurementRow(1, LocalDateTime.now(), LocalDateTime.now(), "meters", "distance measurement", "m"),
      new SystemUnitofmeasurementRow(2, LocalDateTime.now(), LocalDateTime.now(), "kilograms", "weight measurement", "kg"),
      new SystemUnitofmeasurementRow(3, LocalDateTime.now(), LocalDateTime.now(), "meters cubed", "3d spaceq", "m^3"),
      new SystemUnitofmeasurementRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fibaro"),
      new SystemUnitofmeasurementRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "Fibaro"))

    SystemUnitOfMeasurement.forceInsertAll(systemUnitOfMeasurementRows: _*)

    val systemTypeRows = Seq(
      new SystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), "room dimensions", "Fibaro"),
      new SystemTypeRow(2, LocalDateTime.now(), LocalDateTime.now(), "dayily activities", "Fibaro"),
      new SystemTypeRow(3, LocalDateTime.now(), LocalDateTime.now(), "utilities", "Fibaro"),
      new SystemTypeRow(4, LocalDateTime.now(), LocalDateTime.now(), "personattributes", "Fibaro"),
      new SystemTypeRow(5, LocalDateTime.now(), LocalDateTime.now(), "locationattributes", "Fibaro"))

    SystemType.forceInsertAll(systemTypeRows: _*)

    val systemPropertyRows = Seq(
      new SystemPropertyRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity", "Fibaro", 1, 1),
      new SystemPropertyRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse", "Fibaro", 3, 2),
      new SystemPropertyRow(3, LocalDateTime.now(), LocalDateTime.now(), "size", "Fibaro", 1, 3),
      new SystemPropertyRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight", "Fibaro", 2, 4),
      new SystemPropertyRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation", "Fibaro", 4, 5))

    SystemProperty.forceInsertAll(systemPropertyRows: _*)

    val systemPropertyRecordRows = Seq(
      new SystemPropertyrecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "kichenElectricity"),
      new SystemPropertyrecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemPropertyrecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemPropertyrecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      new SystemPropertyrecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation"))

    SystemPropertyRecord.forceInsertAll(systemPropertyRecordRows: _*)

    // Entities
    val thingsThingRows = Seq(
      new ThingsThingRow(1, LocalDateTime.now(), LocalDateTime.now(), "cupbord"),
      new ThingsThingRow(2, LocalDateTime.now(), LocalDateTime.now(), "car"),
      new ThingsThingRow(3, LocalDateTime.now(), LocalDateTime.now(), "shower"),
      new ThingsThingRow(4, LocalDateTime.now(), LocalDateTime.now(), "scales"))

    ThingsThing.forceInsertAll(thingsThingRows: _*)

    val peoplePersonRows = Seq(
      new PeoplePersonRow(1, LocalDateTime.now(), LocalDateTime.now(), "Martin"),
      new PeoplePersonRow(2, LocalDateTime.now(), LocalDateTime.now(), "Andrius"),
      new PeoplePersonRow(3, LocalDateTime.now(), LocalDateTime.now(), "Xiao"))

    PeoplePerson.forceInsertAll(PeoplePersonRows: _*)

    val locationsLocationRows = Seq(
      new LocationsLocationRow(1, LocalDateTime.now(), LocalDateTime.now(), "kitchen"),
      new LocationsLocationRow(2, LocalDateTime.now(), LocalDateTime.now(), "bathroom"),
      new LocationsLocationRow(3, LocalDateTime.now(), LocalDateTime.now(), "WMG"))

    LocationsLocation.forceInsertAll(locationsLocationRows: _*)

    val organisationsOrganisationRows = Seq(
      new OrganisationsOrganisationRow(1, LocalDateTime.now(), LocalDateTime.now(), "seventrent"),
      new OrganisationsOrganisationRow(2, LocalDateTime.now(), LocalDateTime.now(), "WMG")
    )

    OrganisationsOrganisation.forceInsertAll(organisationsOrganisationRows: _*)

    val eventsEventRows = Seq(
      new EventsEventRow(1, LocalDateTime.now(), LocalDateTime.now(), "having a shower"),
      new EventsEventRow(2, LocalDateTime.now(), LocalDateTime.now(), "driving"),
      new EventsEventRow(3, LocalDateTime.now(), LocalDateTime.now(), "going to work"),
      new EventsEventRow(4, LocalDateTime.now(), LocalDateTime.now(), "cooking")
    )

    EventsEvent.forceInsertAll(eventsEventRows: _*)

    val entityRows = Seq(
      new EntityRow(1, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 1, None, None, None),
      new EntityRow(2, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", 1, None, None, None, None),
      new EntityRow(3, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, 1),
      new EntityRow(4, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "organisation", None, None, None, 1, None),
      new EntityRow(5, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 1, None, None),
      new EntityRow(6, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 2, None, None, None),
      new EntityRow(7, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", 2, None, None, None, None),
      new EntityRow(8, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, 2),
      new EntityRow(9, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "organisation", None, None, None, 2, None),
      new EntityRow(10, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 2, None, None),
      new EntityRow(11, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 3, None, None),
      new EntityRow(12, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 3, None, None, None),
      new EntityRow(13, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "location", 3, None, None, None, None),
      new EntityRow(14, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "peron", None, None, None, None, 3),
      new EntityRow(15, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 3, None, None),
      new EntityRow(16, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "event", None, None, 4, None, None),
      new EntityRow(16, LocalDateTime.now(), LocalDateTime.now(), "timestamp", "thing", None, 4, None, None, None)
    )

    Entity.forceInsertAll(entityRows: _*)

    // Relationship Record

    val systemRelationshipRecordRows = Seq(
      new SystemRelationshiprecordRow(1, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshiprecordRow(2, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshiprecordRow(3, LocalDateTime.now(), LocalDateTime.now(), "Colleagues"),
      new SystemRelationshiprecordRow(4, LocalDateTime.now(), LocalDateTime.now(), "weight"),
      new SystemRelationshiprecordRow(5, LocalDateTime.now(), LocalDateTime.now(), "elevation"),
      new SystemRelationshiprecordRow(6, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshiprecordRow(7, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshiprecordRow(8, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemRelationshiprecordRow(9, LocalDateTime.now(), LocalDateTime.now(), "Car Ownership"),
      new SystemRelationshiprecordRow(10, LocalDateTime.now(), LocalDateTime.now(), "elevation"),
      new SystemRelationshiprecordRow(11, LocalDateTime.now(), LocalDateTime.now(), "Driving to Work"),
      new SystemRelationshiprecordRow(12, LocalDateTime.now(), LocalDateTime.now(), "wateruse"),
      new SystemRelationshiprecordRow(13, LocalDateTime.now(), LocalDateTime.now(), "size"),
      new SystemRelationshiprecordRow(14, LocalDateTime.now(), LocalDateTime.now(), "weight")
    )

    SystemRelationshipRecord.forceInsertAll(systemRelationshipRecordRows: _*)


    val eventsEventToEventCrossRefRows = Seq(
      new EventsEventtoeventcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, "Parent_Child", true, 1)
    )

    EventsEventToEventCrossRef.forceInsertAll(eventsEventToEventCrossRefRows: _*)

    val thingsThingToThingCrossRefRows = Seq(
      new ThingsThingtothingcrossrefRow()(1, LocalDateTime.now(), LocalDateTime.now(), 1, 1, "Parent_Child", true, 2)
    )

    ThingsThingToThingCrossRefCrossRef.forceInsertAll(thingsThingToThingCrossRefRows: _*)

    val peoplePersonToPersonCrossRefRows = Seq(
      new PeoplePersontopersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Colleague", true, 3)
    )

    PeoplePersonToPersonCrossRef.forceInsertAll(peoplePersonToPersonCrossRefRows: _*)

    val organisationsOrganisationToOrganisationCrossRefRows = Seq(
      new OrganisationsOrganisationtoorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Buys_From", true, 4)
    )

    OrganisationsOrganisationToOrganisationCrossRef.forceInsertAll(organisationsOrganisationToOrganisationCrossRefRows: _*)

    val locationsLocationToLocationCrossRefRows = Seq(
      new LocationsLocationtolocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Next_To", true, 4)
    )

    LocationsLocationToLocationCrossRef.forceInsertAll(locationsLocationToLocationCrossRefRows: _*)

    // Event Relationships

    val eventsEventToThingCrossRefRows = Seq(
      new Events(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "Used_During", true, 5)
    )


    EventsEventToThingCrossRef.forceInsertAll(EventsEventToThingCrossRefRows: _*)

    val eventsEventToLocationCrossRefRows = Seq(
      new EventsEventlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Is_At", true, 6)
    )


    EventsEventToLocationCrossRef.forceInsertAll(EventsEventToLocationCrossRefRows: _*)

    val eventsEventToPersonCrossRefRows = Seq(
      new EventsEventpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "Is_At", true, 7)
    )


    EventsEventToPersonCrossRef.forceInsertAll(EventsEventToPersonCrossRefRows: _*)

    val eventsEventToOrganisationCrossRefRows = Seq(
      new EventsEventorganisationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, "Uses_Utility", true, 8)
    )


    EventsEventToOrganisationCrossRef.forceInsertAll(EventsEventToOrganisationCrossRefRows: _*)
    //  Thing Relationships

    val thingsThingToPersonCrossRefRows = Seq(
      new ThingsThingpersoncrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "Owns", true, 9)
    )


    ThingsThingToPersonCrossRef.forceInsertAll(ThingsThingToPersonCrossRefRows: _*)

    // Location Relationships

    val locationsLocationToThingCrossRefRows = Seq(
      new LocationsLocationthingcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, "Is_At", true, 10)
    )


    LocationsLocationToThingCrossRef.forceInsertAll(LocationsLocationToThingCrossRefRows: _*)


    // Organisation Relationships

    val organisationOrganisationLocationCrossRefRows = Seq(
      new OrganisationsOrganisationlocationcrossrefRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 3, "Is_At", true, 11)
    )


    OrganisationOrganisationLocationCrossRef.forceInsertAll(OrganisationOrganisationLocationCrossRefRows: _*)


    val organisationOrganisationThingCrossRefRows = Seq(
      new OrganisationOrganisationThingCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 2, "Rents", true, 12)
    )


    OrganisationOrganisationThingCrossRef.forceInsertAll(OrganisationOrganisationThingCrossRefRows: _*)

    //People Relationships

    val peoplePersonOrganisationCrossRefRows = Seq(
      new PeoplePersonOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "Works_at", true, 13)
    )


    PeoplePersonOrganisationCrossRefCrossRef.forceInsertAll(PeoplePersonOrganisationCrossRefRows: _*)


    val peoplePersonOrganisationCrossRefRows = Seq(
      new PeoplePersonOrganisationCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 1, "Is_at", true, 14)
    )


    PeoplePersonOrganisationCrossRef.forceInsertAll(PeoplePersonOrganisationCrossRefRefRows: _*)

    // location Property/type Relationships 

    val locationsSystemPropertyDynamicCrossRefRows = Seq(
      new LocationsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 4, 2, 3, 2, "Parent Child", true, 1)
    )


    LocationsSystemPropertyDynamicCrossRefCrossRef.forceInsertAll(LocationsSystemPropertyDynamicCrossRefRows: _*)

    val locationsSystemPropertyStaticCrossRefRows = Seq(
      new LocationsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 4, 3, 4, "Parent Child", true, 5)
    )


    LocationsSystemPropertyStaticCrossRef.forceInsertAll(LocationsSystemPropertyStaticCrossRefRows: _*)

    val locationsSystemTypeRows = Seq(
      new LocationsSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 2, 1, "recordID", true)
    )


    LocationsSystemTypeCrossRef.forceInsertAll(LocationsSystemTypeRows: _*)

    // things Property/type Relationships

    val thingsSystemPropertyStaticCrossRefRows = Seq(
      new ThingsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 3, 2, 1, "Parent Child", true, 3)
    )


    ThingsSystemPropertyStaticCrossRef.forceInsertAll(LocationsSystemTypeRows: _*)


    val thingsSystemPropertyDynamicCrossRefRows = Seq(
      new ThingsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, 1, 2, "Parent Child", true, 3)

    )


    ThingsSystemPropertyDynamicCrossRef.forceInsertAll(ThingsSystemPropertyDynamicCrossRefRows: _*)

    val thingsSystemTypeCrossRefRows = Seq(
      new ThingsSystemTypeCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), thingID, 2, "recordID", true)
    )


    ThingsThingSystemTypeCrossRef.forceInsertAll(ThingsSystemTypeCrossRefRows: _*)

    // people Property/type Relationships

    val peopleSystemPropertyStaticCrossRefRows = Seq(
      new PeopleSystemPropertyStaticCrossRefRows(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 2, 1, 4, "Parent Child", true, 2)
    )


    PeopleSystemPropertyStaticCrossRef.forceInsertAll(PeopleSystemPropertyStaticCrossRefRows: _*)

    val peopleSystemPropertyDynamicCrossRefRows = Seq(
      new PeopleSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 1, 3, "Parent Child", true, 2)
    )


    PeopleSystemPropertyDynamicCrossRef.forceInsertAll(PeopleSystemPropertyDynamicCrossRefRows: _*)

    val peopleSystemTypeRows = Seq(
      new PeopleSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, "recordID", true)
    )


    PeopleSystemTypeCrossRef.forceInsertAll(PeopleSystemTypeRows: _*)

    // events Property/type Relationships

    val eventsSystemPropertyStaticCrossRefRows = Seq(
      new EventsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 4, 3, 2, 1, "Parent Child", true, 2)
    )


    EventsSystemPropertyStaticCrossRef.forceInsertAll(EventsSystemPropertyStaticCrossRefRows: _*)

    val eventsSystemPropertyDynamicCrossRefRows = Seq(
      new EventsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 3, 2, 5, 2, "Parent Child", true, 3)
    )


    EventsSystemPropertyDynamicCrossRef.forceInsertAll(EventsSystemPropertyDynamicCrossRefRows: _*)

    val eventsSystemTypeRows = Seq(
      new EventsSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 4, "recordID", true)
    )


    EventsSystemTypeRef.forceInsertAll(EventsSystemTypeRows: _*)

    // organisation Property/type Relationships

    val organisationsSystemPropertyStaticCrossRefRows = Seq(
      new OrganisationsSystemPropertyStaticCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, 3, 2, 1, "Parent Child", true, 4)
    )


    OrganisationsSystemPropertyStaticCrossRef.forceInsertAll(OrganisationsSystemPropertyStaticCrossRefRows: _*)

    val organisationsSystemPropertyDynamicCrossRefRows = Seq(
      new OrganisationsSystemPropertyDynamicCrossRefRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 3, 2, 3, "Parent Child", true, 1)
    )

    OrganisationsSystemPropertyDynamicCrossRef.forceInsertAll(OrganisationsSystemPropertyDynamicCrossRefRows: _*)

    val organisationsSystemTypeRows = Seq(
      new OrganisationSystemTypeRow(1, LocalDateTime.now(), LocalDateTime.now(), 1, 2, "recordID", true)
    )

    OrganisationSystemType.forceInsertAll(OrganisationSystemTypeRows: _*)
  }

  def clearAllData(implicit session: Session) = {
    BundleTableslicecondition.delete
    BundleTableslice.delete
    BundleTable.delete

    DataValue.delete
    DataRecord.delete
    DataField.delete
    DataTabletotablecrossref.delete
    DataTable.delete
    SystemProperty.delete
    SystemType.delete
    SystemUnitOfMeasurement.delete
    SystemPropertyRecord.delete
    SystemRelationshipRecord.delete
    EventsSystemPropertyStaticCrossRef.delete
    EventsSystemPropertyDynamicCrossRef.delete
    EventsSystemType.delete
    EventsEventToEventCrossRef.delete
    EventsEventToOrganisationCrossRef.delete
    EventsEventToThingCrossRef.delete
    EventsEventToLocationCrossRef.delete
    EventsEvent.delete
    OrganisationsOrganisation.delete
    OrganisationsOrganisationToOrganisationCrossRef.delete
    OrganisationsSystemType.delete
    OrganisationsSystemPropertyDynamicCrossRef.delete
    OrganisationsSystemPropertyStaticCrossRef.delete
    OrganisationOrganisationThingCrossRef.delete
    OrganisationOrganisationLocationCrossRef.delete
    ThingsThing.delete
    ThingsThingtoThingCrossRef.delete
    ThingsSystemTypeCrossRef.delete
    ThingsSystemPropertyDynamicCrossRef.delete
    ThingsSystemPropertyStaticCrossRef.delete
    ThingsThingToPersonCrossRef.delete
    PeoplePerson.delete
    PeoplePersontoPersonCrossRef.delete
    PeopleSystemTypeCrossRef.delete
    PeopleSystemPropertyDynamicCrossRef.delete
    PeopleSystemPropertyStaticCrossRef.delete
    PeoplePersonOrganisationCrossRef.delete
    PeoplePersonLocationCrossRef.delete
    PeoplePersonOrganisationCrossRef.delete
    LocationsLocation.delete
    LocationsToLocationsCrossRef.delete
    LocationsSystemTypeCrossRef.delete
    LocationsSystemPropertyDynamicCrossRef.delete
    LocationsSystemPropertyStaticCrossRef.delete
    LocationsLocationToThingCrossRef.delete
  }
}