package bookstore.config

object types {
  case class HttpConfig(port: String, host: String) 
  case class DbConfig(some: String) 
  
  case class AppConfig(httpConfig: HttpConfig, dbConfig: DbConfig)  
}