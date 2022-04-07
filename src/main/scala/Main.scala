import com.snowflake.snowpark._
import com.snowflake.snowpark.functions._
object Main {
  def main(args: Array[String]): Unit = {
    val configs = Map (
      "URL" -> s"https://${sys.env("SNOW_ACCOUNT")}.snowflakecomputing.com:443",
      "USER" -> sys.env("SNOW_USER"),
      "PASSWORD" -> sys.env("SNOW_PASSWORD"),
      "ROLE" -> sys.env("SNOW_ROLE"),
      "WAREHOUSE" -> sys.env("SNOW_WAREHOUSE"),
      "DB" -> sys.env("SNOW_DATABASE"),
      "SCHEMA" -> sys.env.getOrElse("SNOW_SCHEMA", "")
    )
    val session = Session.builder.configs(configs).create
    // Test Function
    session.table("companies").select(callUDF("DB1.SCALA_DEMO.linearRegression", callBuiltin("ARRAY_AGG", col("N_EMPLOYEES")), callBuiltin("ARRAY_AGG", col("ANNUAL_REVENUE")), lit(100))).show()
    // Test Stored Procedure
    session.sql("call companyType()").show()
    session.table("processed_company_type").show()
  }
}