package zio.http.codec

import zio.Chunk
import zio.http.codec.HttpCodecError
import zio.prelude._

sealed case class QueryCardinality[F[+_]: Covariant](extract: (String, Chunk[String]) => F[String]) {
  def coerce(values: Any): F[_] = values.asInstanceOf[F[_]]

  def decode[I](name: String, codec: TextCodec[I], values: Chunk[String]): F[I] = extract(name, values) map {
    (value: String) =>
      if (codec.isDefinedAt(value)) codec(value) else throw HttpCodecError.MalformedQueryParam(name, codec)
  }
}

object QueryCardinality {
  object any extends QueryCardinality((_, values) => values)

  object oneOrMore
      extends QueryCardinality((name, values) =>
        values
          .nonEmptyOrElse(throw HttpCodecError.WrongQueryParamCardinality(name, values.length, "one or more"))(identity),
      )

  object optional
      extends QueryCardinality((name, values) =>
        if (values.length > 1) throw HttpCodecError.WrongQueryParamCardinality(name, values.length, "one or none")
        else values.headOption,
      )

  object one
      extends QueryCardinality[Id.Type]((name, values) =>
        if (values.length == 1) Id(values.head)
        else throw HttpCodecError.WrongQueryParamCardinality(name, values.length, "exactly one"),
      )
}
