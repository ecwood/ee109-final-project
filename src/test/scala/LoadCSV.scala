import spatial.dsl._

@spatial class LoadCSV extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]
  type InstructionFixed = FixPt[FALSE, _24, _0]

  @struct class Instruction(
    a: FixPt[FALSE, _1, _0],
    gg: FixPt[FALSE, _2, _0],
    oo: FixPt[FALSE, _2, _0],
    src1: FixPt[FALSE, _4, _0],
    src2: FixPt[FALSE, _4, _0],
    dest: FixPt[FALSE, _4, _0]
  )

  @struct class Vector3(
    elem1: RegType,
    elem2: RegType,
    elem3: RegType
  )

  // Number of instructions in the file (need a way for this to be dynamic)
  val N = 1
  val pixel_rows = 100
  val pixel_columns = 100
  val registers = 16

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[InstructionFixed](s"$DATA/load_csv_test_1.csv")
    val inst_dram = DRAM[InstructionFixed](N)

    setMem(inst_dram, inst_host)

    val out = ArgOut[Int]

    Accel {
      val inst_sram = SRAM[InstructionFixed](N)
      inst_sram load inst_dram

      out := 1 + 1
    }

    val result = getArg(out)

    // temporary for basic asm tests
    print(result)
    assert(result == 2)
  }
}
