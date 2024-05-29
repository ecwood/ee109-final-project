import spatial.dsl._

@spatial class LoadCSV extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]
  type Bit = FixPt[FALSE, _1, _0]
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
  val num_instructions = 8
  val instruction_bits = 24
  val pixel_rows = 100
  val pixel_columns = 100
  val registers = 16

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[Bit](s"$DATA/load_csv_test_1.csv")
    val inst_dram = DRAM[Bit](num_instructions, instruction_bits)

    setMem(inst_dram, inst_host)

    val out = DRAM[Int](num_instructions)

    Accel {
      val inst_sram = SRAM[Bit](num_instructions, instruction_bits)
      inst_sram load inst_dram

      val out_folder = SRAM[Int](num_instructions)

      Foreach (num_instructions by 1) {j => out_folder(j) = 0}

      MemFold(out_folder)(0 until instruction_bits by 1) { i =>
        val tmp = SRAM[Int](num_instructions)
        Foreach(num_instructions by 1) {j =>
          tmp(j) = inst_sram(j, i).to[Int]
        }
        tmp
      }{_+_}

      out store out_folder
    }

    val result = getMem(out)

    // temporary for basic asm tests
    print(result)
    // assert(result == [1])
  }
}
