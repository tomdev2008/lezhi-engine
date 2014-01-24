package com.buzzinate.dm.seq

import com.nicta.scoobi.io.OutputConverter
import com.nicta.scoobi.io.DataSink
import org.apache.hadoop.fs.Path
import org.apache.hadoop.mapreduce.lib.output.SequenceFileOutputFormat
import com.nicta.scoobi.application.ScoobiConfiguration
import com.nicta.scoobi.io.Helper
import org.slf4j.LoggerFactory
import org.apache.hadoop.fs.FileAlreadyExistsException
import org.apache.hadoop.mapreduce.Job
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat
import org.apache.hadoop.io.compress.BZip2Codec
import org.apache.hadoop.io.SequenceFile
import org.apache.commons.logging.LogFactory

 /* Class that abstracts all the common functionality of persisting to sequence files. */
class SeqSink[K, V](path: String, overwrite: Boolean)(implicit mk: Manifest[K], mv: Manifest[V]) extends DataSink[K, V, (K, V)] {

    protected val outputPath = new Path(path)

    val outputFormat = classOf[SequenceFileOutputFormat[K, V]]
    val outputKeyClass = mk.erasure.asInstanceOf[Class[K]]
    val outputValueClass = mv.erasure.asInstanceOf[Class[V]]
    
    val converter = new OutputConverter[K, V, (K, V)] {
      def toKeyValue(kv: (K, V)) = (kv._1, kv._2)
    }

    def outputCheck(sc: ScoobiConfiguration) {
      if (Helper.pathExists(outputPath)(sc))
        if (overwrite) {
          SeqSink.logger.info("Deleting the pre-existing output path: " + outputPath.toUri.toASCIIString)
          Helper.deletePath(outputPath)(sc)
        } else {
          throw new FileAlreadyExistsException("Output path already exists: " + outputPath)
        }
      else
        SeqSink.logger.info("Output path: " + outputPath.toUri.toASCIIString)
    }

    def outputConfigure(job: Job) {
      FileOutputFormat.setCompressOutput(job, true)
      FileOutputFormat.setOutputCompressorClass(job, classOf[BZip2Codec])
      SequenceFileOutputFormat.setOutputCompressionType(job, SequenceFile.CompressionType.BLOCK)
      FileOutputFormat.setOutputPath(job, outputPath)
    }

    lazy val outputConverter = converter
  }

object SeqSink {
  lazy val logger = LogFactory.getLog("scoobi.seqsink")
}