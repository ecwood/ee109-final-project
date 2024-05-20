import spatial.dsl._

@spatial class RayTracer extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]

  @struct class Instruction(
    key: FixPt[FALSE, _5, _0],
    src1: FixPt[FALSE, _4, _0],
    src2: FixPt[FALSE, _4, _0],
    dest: FixPt[FALSE, _4, _0]
  )

  @struct class Vector3(
    elem1: RegType,
    elem2: RegType,
    elem3: RegType
  )

  val N = 1024

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[Instruction](s"$DATA/instructions.csv")
    val inst_dram = DRAM[Instruction](N)

    setMem(inst_dram, inst_host)

    Accel {
      val vec_reg0 = Reg[Vector3]
      val vec_reg1 = Reg[Vector3]
      val vec_reg2 = Reg[Vector3]
      val vec_reg3 = Reg[Vector3]
      val vec_reg4 = Reg[Vector3]
      val vec_reg5 = Reg[Vector3]
      val vec_reg6 = Reg[Vector3]
      val vec_reg7 = Reg[Vector3]
      val vec_reg8 = Reg[Vector3]
      val vec_reg9 = Reg[Vector3]
      val vec_reg10 = Reg[Vector3]
      val vec_reg11 = Reg[Vector3]
      val vec_reg12 = Reg[Vector3]
      val vec_reg13 = Reg[Vector3]
      val vec_reg14 = Reg[Vector3]
      val vec_reg15 = Reg[Vector3]

      val sca_reg0 = Reg[RegType]
      val sca_reg1 = Reg[RegType]
      val sca_reg2 = Reg[RegType]
      val sca_reg3 = Reg[RegType]
      val sca_reg4 = Reg[RegType]
      val sca_reg5 = Reg[RegType]
      val sca_reg6 = Reg[RegType]
      val sca_reg7 = Reg[RegType]
      val sca_reg8 = Reg[RegType]
      val sca_reg9 = Reg[RegType]
      val sca_reg10 = Reg[RegType]
      val sca_reg11 = Reg[RegType]
      val sca_reg12 = Reg[RegType]
      val sca_reg13 = Reg[RegType]
      val sca_reg14 = Reg[RegType]
      val sca_reg15 = Reg[RegType]
    }

  }
}