import spatial.dsl._

@spatial class SquareRoot extends SpatialTest {
  type RegType = FixPt[TRUE, _24, _8]
  type InstructionFixed = FixPt[FALSE, _24, _0]
  type InstBit = FixPt[FALSE, _8, _0]
  type SubType = FixPt[TRUE, _23, _8]

  @struct class Vector3(
    x: RegType,
    y: RegType,
    z: RegType
  )
  val iterations = 31
  val nums = 80

  def main(args: Array[String]): Unit = {
    val out = DRAM[RegType](nums)

    Accel {
      val temp = 26.0.to[RegType]
      val square_roots = SRAM[RegType](nums)

      Foreach (1 until nums + 1) { num =>
        val actual_square_root = Reg[RegType](0.0)
        val square_num = num.to[RegType]
        Reduce (actual_square_root) (iterations by 1) { base_square =>
          val square = base_square + 1
          val squared = (square * square).to[RegType]
          val square_cast = square.to[RegType]
          val divided = (square_num.to[SubType] / squared.to[SubType]).to[RegType]
          val shfted = (divided.to[SubType] - 1.0).to[RegType]
          val shfted_sqed = (shfted.to[SubType] * shfted.to[SubType]).to[RegType]
          val shfted_tred = (shfted.to[SubType] * shfted.to[SubType] * shfted.to[SubType]).to[RegType]
          val sqrt_elem1 = 1.0.to[RegType]
          val sqrt_elem2 = (shfted.to[SubType] / 2.0).to[RegType]
          val sqrt_elem3 = (shfted_sqed.to[SubType] / 4.0).to[RegType]
          val sqrt_elem4 = (shfted_tred.to[SubType] / 16.0).to[RegType]
          val sqrt_series = (sqrt_elem1.to[SubType] + sqrt_elem2.to[SubType] - sqrt_elem3.to[SubType] + sqrt_elem4.to[SubType]).to[RegType]
          val square_root = (square_cast.to[SubType] * sqrt_series.to[SubType]).to[SubType]
          val next = (square_cast.to[SubType] + 1.to[SubType]).to[RegType]
          val next_squared = (next.to[SubType] * next.to[SubType]).to[RegType]
          val next_divided = (square_num.to[SubType] / next_squared.to[SubType]).to[RegType]
          val save = (divided >= 1 && (next_divided < 1)).to[RegType]
          val value_to_save = (square_root.to[RegType] * save).to[RegType]
          value_to_save.to[RegType]
        } {_+_}
        square_roots(num - 1) = actual_square_root
      }

      out store square_roots
    }

    val result = getMem(out)

    // temporary for basic asm tests
    printArray(result)
    assert(1 == 1)
  }
}