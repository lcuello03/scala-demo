package com.mobilize.spark

import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.{Column, DataFrame}

object Main {

  def main(args: Array[String]): Unit = {
    SessionDriver.write(project(
      SessionDriver.read("collateralAsset")
      , SessionDriver.read("collateralAgreement")
      , SessionDriver.read("collateralAgreementAsset")
    ),"CustomerSegmentationCollateralAgreement")

    SessionDriver.read("CustomerSegmentationCollateralAgreement").show()
  }

  def project(
               collateralAsset: DataFrame,
               collateralAgreement: DataFrame,
               collateralAgreementAsset: DataFrame): DataFrame = {

    var result = collateralAgreementAsset
      .join(
        collateralAsset,
        Seq(CollateralAsset.collateral_asset_id.name, CollateralAsset.collateral_asset_start_datetime.name),
        "left"
      )
      .join(
        collateralAgreement,
        Seq(CollateralAgreementAsset.collateral_agreement_id.name, CollateralAgreementAsset.collateral_agreement_start_datetime.name),
        "left"
      )

    result = result.groupBy(
      CollateralAgreementAsset.collateral_agreement_id.col,
      CollateralAgreementAsset.collateral_agreement_start_datetime.col,
      CollateralAgreementAsset.collateral_agreement_asset_end_datetime.col,
      CollateralAgreement.collateral_agreement_reporting_amount.col
    )
      .agg(
        sum(
          when(
            CollateralAsset.collateral_asset_basel_product_code.col === lit("640") &&
              CollateralAsset.asset_eligibility_flag_rirb_airb.col === lit(true) &&
              CollateralAgreementAsset.allocated_agreement_asset_reporting_amount.col > lit(BigDecimal(0)),
            CollateralAgreementAsset.allocated_agreement_asset_reporting_amount.col//.cast(defaultDecimalType)
          ).otherwise(lit(BigDecimal(0)))
        )
          //.cast(defaultDecimalType)
          .as(CustomerSegmentationCollateralAgreement.customer_segmentation_collateral_agreement_re_reporting_amount.name),
        when(
          CollateralAgreement.collateral_agreement_reporting_amount.col === lit(null) || CollateralAgreement.collateral_agreement_reporting_amount.col <= lit(BigDecimal(0)),
          lit(BigDecimal(0))
        ).otherwise(
          sum(
            when(
              CollateralAsset.collateral_asset_basel_product_code.col === lit("640") && CollateralAsset.asset_eligibility_flag_rirb_airb.col === lit(true),
              CollateralAgreementAsset.allocated_agreement_asset_reporting_amount.col
            ).otherwise(lit(BigDecimal(0)))
          ) / CollateralAgreement.collateral_agreement_reporting_amount.col
        ).as("Custom column")
      )
    result
      .withColumn(CustomerSegmentationCollateralAgreement.collateral_agreement_end_datetime.name, CollateralAgreementAsset.collateral_agreement_asset_end_datetime.col)
      .withColumn(CustomerSegmentationCollateralAgreement.customer_segmentation_collateral_agreement_total_reporting_amount.name, CollateralAgreement.collateral_agreement_reporting_amount.col
      )
  }
}

case class SparkFieldMeta(`type`: DataType, nullable: Boolean)

case class Field(name: String, col: Column, fieldMeta: SparkFieldMeta)

object CollateralAsset {
  val collateral_asset_id: Field = Field(
    "collateral_asset_id"
    , col("collateral_asset_id")
    , SparkFieldMeta(StringType, nullable = false)
  )
  val collateral_asset_start_datetime: Field = Field(
    "collateral_asset_start_datetime"
    , col("collateral_asset_start_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )
  val collateral_asset_basel_product_code: Field = Field(
    "collateral_asset_basel_product_code"
    , col("collateral_asset_basel_product_code")
    , SparkFieldMeta(IntegerType, nullable = true)
  )
  val asset_eligibility_flag_rirb_airb: Field = Field(
    "asset_eligibility_flag_rirb_airb"
    , col("asset_eligibility_flag_rirb_airb")
    , SparkFieldMeta(BooleanType, nullable = true)
  )
}

object CollateralAgreementAsset { // Documenting this temporarily extends SchemaAware[BuffinancialTransactionS40portionV433Deduplicated] {
  val collateral_agreement_id: Field = Field(
    "collateral_agreement_id"
    , col("collateral_agreement_id")
    , SparkFieldMeta(StringType, nullable = false)
  )
  val collateral_asset_id: Field = Field(
    "collateral_asset_id"
    , col("collateral_asset_id")
    , SparkFieldMeta(StringType, nullable = true)
  )
  val collateral_asset_start_datetime: Field = Field(
    "collateral_asset_start_datetime"
    , col("collateral_asset_start_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )
  val collateral_agreement_start_datetime: Field = Field(
    "collateral_agreement_start_datetime"
    , col("collateral_agreement_start_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )
  val collateral_agreement_asset_end_datetime: Field = Field(
    "collateral_agreement_asset_end_datetime"
    , col("collateral_agreement_asset_end_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )
  val allocated_agreement_asset_reporting_amount: Field = Field(
    "allocated_agreement_asset_reporting_amount"
    , col("allocated_agreement_asset_reporting_amount")
    , SparkFieldMeta(DecimalType(28,2), nullable = true)
  )
}

object CollateralAgreement { // Documenting this temporarily extends SchemaAware[BuffinancialTransactionS40portionV433Deduplicated] {
  val collateral_agreement_id: Field = Field(
    "collateral_agreement_id"
    , col("collateral_agreement_id")
    , SparkFieldMeta(StringType, nullable = false)
  )
  val collateral_agreement_reporting_amount: Field = Field(
    "collateral_agreement_reporting_amount"
    , col("collateral_agreement_reporting_amount")
    , SparkFieldMeta(DecimalType(28,1), nullable = true)
  )
  val collateral_agreement_start_datetime: Field = Field(
    "collateral_agreement_start_datetime"
    , col("collateral_agreement_start_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )
}

object CustomerSegmentationCollateralAgreement { // Documenting this temporarily extends SchemaAware[BuffinancialTransactionS40portionV433Deduplicated] {
  val collateral_agreement_id: Field = Field(
    "collateral_agreement_id"
    , col("collateral_agreement_id")
    , SparkFieldMeta(StringType, nullable = false)
  )
  val collateral_agreement_start_datetime: Field = Field(
    "collateral_agreement_start_datetime"
    , col("collateral_agreement_start_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )
  val customer_segmentation_collateral_agreement_re_reporting_amount: Field = Field(
    "customer_segmentation_collateral_agreement_re_reporting_amount"
    , col("customer_segmentation_collateral_agreement_re_reporting_amount")
    , SparkFieldMeta(DecimalType(28,1), nullable = true)
  )
  val customer_segmentation_collateral_agreement_total_reporting_amount: Field = Field(
    "customer_segmentation_collateral_agreement_total_reporting_amount"
    , col("customer_segmentation_collateral_agreement_total_reporting_amount")
    , SparkFieldMeta(DecimalType(28,1), nullable = true)
  )
  val segm_collateral_agreement_re_ratio: Field = Field(
    "segm_collateral_agreement_re_ratio"
    , col("segm_collateral_agreement_re_ratio")
    , SparkFieldMeta(DecimalType(28,1), nullable = true)
  )
  val collateral_agreement_end_datetime: Field = Field(
    "collateral_agreement_end_datetime"
    , col("collateral_agreement_end_datetime")
    , SparkFieldMeta(DateType, nullable = true)
  )

  def schema: StructType = {
    StructType(
      Seq(
        StructField(collateral_agreement_id.name, collateral_agreement_id.fieldMeta.`type`, collateral_agreement_id.fieldMeta.nullable)
        , StructField(collateral_agreement_start_datetime.name, collateral_agreement_start_datetime.fieldMeta.`type`, collateral_agreement_start_datetime.fieldMeta.nullable)
        , StructField(customer_segmentation_collateral_agreement_re_reporting_amount.name, customer_segmentation_collateral_agreement_re_reporting_amount.fieldMeta.`type`, customer_segmentation_collateral_agreement_re_reporting_amount.fieldMeta.nullable)
        , StructField(customer_segmentation_collateral_agreement_total_reporting_amount.name, customer_segmentation_collateral_agreement_total_reporting_amount.fieldMeta.`type`, customer_segmentation_collateral_agreement_total_reporting_amount.fieldMeta.nullable)
        , StructField(segm_collateral_agreement_re_ratio.name, segm_collateral_agreement_re_ratio.fieldMeta.`type`, segm_collateral_agreement_re_ratio.fieldMeta.nullable)
        , StructField(collateral_agreement_end_datetime.name, collateral_agreement_end_datetime.fieldMeta.`type`, collateral_agreement_end_datetime.fieldMeta.nullable)
      )
    )
  }
}
