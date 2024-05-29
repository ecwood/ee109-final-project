// import spatial.dsl._

// @spatial class RayTracer extends SpatialTest {
//   type RegType = FixPt[FALSE, _16, _0]

//   @struct class Instruction(
//     a: FixPt[FALSE, _1, _0],
//     gg: FixPt[FALSE, _2, _0],
//     oo: FixPt[FALSE, _2, _0],
//     src1: FixPt[FALSE, _4, _0],
//     src2: FixPt[FALSE, _4, _0],
//     dest: FixPt[FALSE, _4, _0]
//   )

//   @struct class Vector3(
//     elem1: RegType,
//     elem2: RegType,
//     elem3: RegType
//   )

//   // Number of instructions in the file (need a way for this to be dynamic)
//   val N = 1024
//   val pixel_rows = 100
//   val pixel_columns = 100
//   val registers = 16

//   def main(args: Array[String]): Unit = {
//     val inst_host = loadCSV1D[Instruction](s"$DATA/instructions.csv")
//     val inst_dram = DRAM[Instruction](N)

//     setMem(inst_dram, inst_host)

//     val out = DRAM[Vector3](pixel_rows, pixel_columns, registers)

//     Accel {
//       // Create the registers for each pixel
//       val vec_regs = SRAM[Vector3](pixel_rows, pixel_columns, registers)
//       val sca_regs = SRAM[RegType](pixel_rows, pixel_columns, registers)

//       val inst_sram = SRAM[Instruction](N)
//       inst_sram load inst_dram

//       Foreach (0 until pixel_rows) { row =>
//         Foreach (0 until pixel_columns) { col =>
//           // Create registers for working with the values after they've been decoded
//           val vec_src1_val = Reg[Vector3]
//           val sca_src1_val = Reg[RegType]
//           val vec_src2_val = Reg[Vector3]
//           val sca_src2_val = Reg[RegType]

//           // Also create registers for saving current destination register values (so that you can store both without
//           // checking which one should be stored)
//           val vec_dest_val = Reg[Vector3]
//           val sca_dest_val = Reg[RegType]

//           // Go through each instruction (pipeline this later)
//           Foreach (0 until N) { i =>
//             Sequential {
//               if (dest == 0) {
//                 vec_regs(row, col, dest) = Vector3(0, 0, 0)
//                 sca_regs(row, col, dest) = 0
//               }
//               if (dest == 1) {
//                 vec_regs(row, col, dest) = Vector3(row, col, 0)
//               }
//               // Fetch Current Instruction and Parse
//               val current_inst = inst_sram(i)
//               val a = current_inst.a
//               val gg = current_inst.gg
//               val oo = current_inst.oo
//               val src1 = current_inst.src1
//               val src2 = current_inst.src2
//               val dest = current_inst.dest
//               val imm = src1 + 16 * oo // need all 6 digits, have to shift the oo by 4; TODO: sign extend

//               // Decode (into both scalar and vector, just in case)
//               // Source 1
//               vec_src1_val = vec_regs(row, col, src1)
//               sca_src1_val = sca_regs(row, col, src1)

//               // Source 2
//               vec_src2_val = vec_regs(row, col, src2)
//               sca_src2_val = sca_regs(row, col, src2)

//               // Destination
//               vec_dest_val = vec_regs(row, col, dest)
//               sca_dest_val = sca_regs(row, col, dest)

//               // Execute Operation
//               // add
//               if (a == 0 && gg == 0 && oo == 0) {
//                 vec_dest_val.elem1 = vec_src1_val.elem1 + vec_src2_val.elem1
//                 vec_dest_val.elem2 = vec_src1_val.elem2 + vec_src2_val.elem2
//                 vec_dest_val.elem3 = vec_src1_val.elem3 + vec_src2_val.elem3
//               }

//               // subtract
//               if (a == 0 && gg == 0 && oo == 1) {
//                 vec_dest_val.elem1 = vec_src1_val.elem1 - vec_src2_val.elem1
//                 vec_dest_val.elem2 = vec_src1_val.elem2 - vec_src2_val.elem2
//                 vec_dest_val.elem3 = vec_src1_val.elem3 - vec_src2_val.elem3
//               }

//               // normalize
//               if (a == 0 && gg == 0 && oo == 2) {
//                 val magnitude = sqrt(vec_src1_val.elem1 * vec_src1_val.elem1 + vec_src1_val.elem2 * vec_src1_val.elem2 + vec_src1_val.elem3 * vec_src1_val.elem3)
//                 vec_dest_val.elem1 = vec_src1_val.elem1 / magnitude
//                 vec_dest_val.elem2 = vec_src1_val.elem2 / magnitude
//                 vec_dest_val.elem3 = vec_src1_val.elem3 / magnitude
//               }

//               // magnitude
//               if (a == 0 && gg == 1 && oo == 0) {
//                 sca_dest_val = sqrt(vec_src1_val.elem1 * vec_src1_val.elem1 + vec_src1_val.elem2 * vec_src1_val.elem2 + vec_src1_val.elem3 * vec_src1_val.elem3)
//               }

//               // magnitude squared
//               if (a == 0 && gg == 1 && oo == 1) {
//                 sca_dest_val = vec_src1_val.elem1 * vec_src1_val.elem1 + vec_src1_val.elem2 * vec_src1_val.elem2 + vec_src1_val.elem3 * vec_src1_val.elem3
//               }

//               // dot product
//               if (a == 0 && gg == 1 && oo == 2) {
//                 sca_dest_val = vec_src1_val.elem1 * vec_src2_val.elem1 + vec_src1_val.elem2 * vec_src2_val.elem2 + vec_src1_val.elem3 * vec_src2_val.elem3
//               }

//               // scalar multiply
//               if (a == 0 && gg == 2 && oo == 0) {
//                 vec_dest_val.elem1 = vec_src1_val.elem1 * sca_src2_val
//                 vec_dest_val.elem2 = vec_src1_val.elem2 * sca_src2_val
//                 vec_dest_val.elem3 = vec_src1_val.elem3 * sca_src2_val
//               }

//               // scalar divide
//               if (a == 0 && gg == 2 && oo == 1) {
//                 vec_dest_val.elem1 = vec_src1_val.elem1 / sca_src2_val
//                 vec_dest_val.elem2 = vec_src1_val.elem2 / sca_src2_val
//                 vec_dest_val.elem3 = vec_src1_val.elem3 / sca_src2_val
//               }

//               // sqrt
//               if (a == 0 && gg == 3 && oo == 0) {
//                 sca_dest_val = sqrt(sca_src2_val)
//               }

//               if (a == 1) {
//                 // addi
//                 if (gg == 0) {
//                   sca_dest_val = sca_src2_val + imm
//                 }

//                 // vaddi.x
//                 if (gg == 1) {
//                   vec_dest_val.elem1 = sca_src2_val + imm
//                 }

//                 // vaddi.y
//                 if (gg == 2) {
//                   vec_dest_val.elem2 = sca_src2_val + imm
//                 }

//                 // vaddi.z
//                 if (gg == 3) {
//                   vec_dest_val.elem2 = sca_src2_val + imm
//                 }
//               }

//               // Store the results if it's not the zero register or first vector register
//               if (dest != 0 && dest != 1) {
//                 vec_regs(row, col, dest) = vec_dest_val
//               }
//               if (dest != 0) {
//                 sca_regs(row, col, dest) = sca_dest_val
//               }
//             }
//           }
//         }
//       }

//       out store vec_regs
//     }

//     val result = getMem(out)

//     // temporary for basic asm tests
//     val check_val = result(0, 0, 1);
//     assert(check_val.elem1 == 1);
//     assert(check_val.elem1 == 2);
//     assert(check_val.elem1 == 3);
//   }
// }
