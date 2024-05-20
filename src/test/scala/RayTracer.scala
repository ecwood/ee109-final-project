import spatial.dsl._

@spatial class RayTracer extends SpatialTest {
  @struct class Instruction(
    key: FixPt[FALSE, _5, _0],
    src1: FixPt[FALSE, _4, _0],
    src2: FixPt[FALSE, _4, _0],
    dest: FixPt[FALSE, _4, _0]
  )

  val N = 1024

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[Instruction](s"$DATA/instructions.csv")
    val inst_dram = DRAM[Instruction](N)

    setMem(inst_dram, inst_host)

    Accel {
      
      
    }

  }
}
