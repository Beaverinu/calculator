import org.mariuszgromada.math.mxparser.*
fun main() {
 License.iConfirmNonCommercialUse("john")
 println(Expression("solve(x^3-8, x, -10, 10)").calculate())
 mXparser.setToComplexMode()
 println(Expression("solve(x^3-8, x, -10, 10)").calculate())
}
