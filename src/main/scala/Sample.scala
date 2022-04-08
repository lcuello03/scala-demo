package Sample
import com.snowflake.snowpark._
import com.snowflake.snowpark.functions._
object Sample {
    def companyType(sess: Session): String = {
        import sess.implicits._
        val qualifyCompany = udf((numberEmployee: Int) =>
            if (numberEmployee < 0) "ERROR"
            else if (numberEmployee < 100) "Small Company"
            else if (numberEmployee < 1000) "Medium Company"
            else "Large Company"
        )
        sess.table("companies")
            .withColumn("COMPANY_TYPE", qualifyCompany('n_employees))
            .write.saveAsTable("processed_company_type")
        "Procedure Executed Successfully!"
    }
}
