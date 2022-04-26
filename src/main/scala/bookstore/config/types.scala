package bookstore.config

object types {
  case class HttpConfig(port: String, host: String) 
  case class DbConfig(driver: String, dbName: String, userName: String, password: String)
  case class RedisConfig(redisPort: String)

  case class AppConfig(httpConfig: HttpConfig, dbConfig: DbConfig, redisConfig: RedisConfig)
}
