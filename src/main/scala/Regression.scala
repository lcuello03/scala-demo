package Regressions
object Regressions {
    def linearRegression(X: Array[Double], Y: Array[Double], valueToPredict: Double): Double = {
        val n = X.length
        var meanX: Double = 0
        var meanY: Double = 0
        var SS_xy: Double = 0
        var SS_xx: Double = 0
        for(index <- 0 to X.length - 1){
            meanX = meanX + X(index)
            meanY = meanY + Y(index)
            SS_xy = SS_xy + X(index) * Y(index)
            SS_xx = SS_xx + X(index) * X(index)
        }
        meanX = meanX / n
        meanY = meanY / n
        SS_xy = SS_xy - n * meanX * meanY
        SS_xx = SS_xx - n * meanX * meanX
        val b_1 = SS_xy / SS_xx
        val b_0 = meanY - b_1 * meanX
        b_0 + b_1 * valueToPredict
    }
}