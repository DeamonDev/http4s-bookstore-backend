package bookstore.domain


object users { 


  case class User(userId: Long,
                  username: String,
                  password: String,
                  firstName: String,
                  lastName: String,
                  verified: Boolean)

  case class UserRegistration(username: String, 
                              password: String,
                              firstName: String,
                              lastName: String,
                              email: String,
                              verified: Boolean)
}