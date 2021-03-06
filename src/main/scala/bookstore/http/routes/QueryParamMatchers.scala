package bookstore.http.routes

import org.http4s.dsl.impl.QueryParamDecoderMatcher

object QueryParamMatchers {

  object AuthorQueryParamMatcher
      extends QueryParamDecoderMatcher[Long]("author_id")
  object LimitQueryParamMatcher extends QueryParamDecoderMatcher[Int]("limit")
}
