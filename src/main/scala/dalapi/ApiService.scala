package dalapi

import akka.actor.{ActorRefFactory, ActorLogging}
import dalapi.service._
import hat.authentication.HatAuthHandler
import hat.authentication.authenticators.{UserPassHandler, AccessTokenHandler}
import spray.routing.HttpServiceActor

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class ApiService extends HttpServiceActor with ActorLogging {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  override def actorRefFactory = context

  // Initialise all the service the actor handles
  val helloService = new HelloService {
    def actorRefFactory = context
  }

  val apiDataService = new DataService {
    def actorRefFactory = context
  }

  val apiPropertyService = new PropertyService {
    def actorRefFactory = context
    val dataService = apiDataService
  }

  val bundleService = new BundleService {
    def actorRefFactory = context
    val dataService = apiDataService
  }

  val eventsService = new EventsService {
    def actorRefFactory = context
    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val locationsService = new LocationsService {
    def actorRefFactory = context
    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val peopleService = new PeopleService {
    def actorRefFactory = context
    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val thingsService = new ThingsService {
    def actorRefFactory = context
    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  val organisationsService = new OrganisationsService {
    def actorRefFactory = context
    val dataService = apiDataService
    val propertyService = apiPropertyService
  }

  // Concatenate all their handled routes
  val routes = helloService.routes ~
    apiDataService.routes ~
    apiPropertyService.routes ~
    bundleService.routes ~
    eventsService.routes ~
    locationsService.routes ~
    peopleService.routes ~
    thingsService.routes ~
    organisationsService.routes

  def receive = runRoute(routes)
}
