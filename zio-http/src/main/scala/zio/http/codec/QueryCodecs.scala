/*
 * Copyright 2021 - 2023 Sporta Technologies PVT LTD & the ZIO HTTP contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zio.http.codec
import zio.{Chunk, NonEmptyChunk}
import zio.prelude.{ForEach, Id}
import zio.stacktracer.TracingImplicits.disableAutoTrace
private[codec] trait QueryCodecs {
  @inline def queryAs[A](name: String)(implicit codec: TextCodec[A]): QueryCodec[A] =
    HttpCodec.MonoQuery(name, codec)

  def query(name: String): QueryCodec[String] = queryAs[String](name)

  def queryBool(name: String): QueryCodec[Boolean] = queryAs[Boolean](name)

  def queryInt(name: String): QueryCodec[Int] = queryAs[Int](name)

  @inline def queryAs[F[+_]: ForEach, I](name: String, cardinality: QueryCardinality[F])(implicit
    codec: TextCodec[I],
  ): QueryCodec[F[I]] =
    HttpCodec.MultiQuery(name, codec, cardinality)

  def queryOpt[I: TextCodec](name: String): QueryCodec[Option[I]] = queryAs(name, QueryCardinality.optional)

  def queryOne[I: TextCodec](name: String): QueryCodec[Id[I]] = queryAs(name, QueryCardinality.one)

  def queries[I: TextCodec](name: String): QueryCodec[Chunk[I]] = queryAs(name, QueryCardinality.any)

  def queryOneOrMore[I: TextCodec](name: String): QueryCodec[NonEmptyChunk[I]] =
    queryAs(name, QueryCardinality.oneOrMore)

  def paramAs[A](name: String)(implicit codec: TextCodec[A]): QueryCodec[A] = queryAs[A](name)

  def paramStr(name: String): QueryCodec[String] = query(name)

  def paramBool(name: String): QueryCodec[Boolean] = queryBool(name)

  def paramInt(name: String): QueryCodec[Int] = queryInt(name)

  def paramAs[F[+_]: ForEach, I: TextCodec](name: String, cardinality: QueryCardinality[F]): QueryCodec[F[I]] =
    queryAs(name, cardinality)

  def paramOpt[I: TextCodec](name: String): QueryCodec[Option[I]] = queryOpt(name)

  def paramOne[I: TextCodec](name: String): QueryCodec[Id[I]] = queryOne(name)

  def params[I: TextCodec](name: String): QueryCodec[Chunk[I]] = queries(name)

  def paramOneOrMore[I: TextCodec](name: String): QueryCodec[NonEmptyChunk[I]] = queryOneOrMore(name)
}
