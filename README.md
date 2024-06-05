# Optimizations

## What optimizations have you tried?
We performed all our our optimization testing on a 10x10 pixel, 79 instruction sample. This sample had a manageable initial length (rather than 30 minutes for a 50x50 grid to 1.5 hours for a 100x100 grid), so it was ideal for testing the influence of optimizations. One flaw with this, however, is that, with the larger grids, the square root loading becomes less important to both cycles/iter and BRAM. Therefore, our tests do not scale as well as we expected.

Additionally, all of our data comes from Scalasim values (and the `compute_resource_utilization.py` script). We had significant issues with VCS and ZCU due to the memory intensity of our system. Please see [this section](#issues-with-vcs-and-zcu) for more information about these issues.

The optimization variables that we considered were
- Parallelization of the row loop (i.e. the foreach row controller)
- Parallelization of the column loop (i.e. the foreach column controller)
- Parallelization of the square root loading (i.e. the foreach square root table element controller)
- Times the input data is saved (every loop or only the last loop)
- SRAM square load chunks (i.e. how much of `square.csv` to load into SRAM at once and, eventually, how many things to load in general)

Initially, we imagined that the first three would be the most significant, since they were clearly parallelizable loops. In particular, the row and column loops are designed to be run in parallel since this is a GPU style system, where every instruction is run on every pixel. However, they were less influential that we expected.

After examining the cycle data, we noticed that saves to our output (not necessarily DRAM stores, but just to the SRAM that eventually is output) dominated. This was because we were outputting vector register 1 for every pixel and every instruction. While having the data from every instruction was instrumental in testing, we realized that it was unnecessary for a final version. Therefore, we switched it to only at the end and significantly improved both our BRAM and cycle count usage.

After this, we tried increasing the parallelization of the row loop (it was already at `par 5` to speed up testing). This drastically increased our BRAM but improved our cycles/iter slightly. Once we added in parallelization of the column loop, we realized that this was not an effective strategy (the BRAM usage skyrocketed as did the runtime) while only modestly reducing our cycles/iter. This strategy was clearly not working.

Finally, we recognized our cycles/iter was significantly impacted by the loading of the SRAM square map. First, we attempted to do this in pieces (chunks of 32). This increased our cycles/iter as well as our BRAM usage. Then, we tried parallelizing this (`par 32`), but this led our runtime to skyrocket (while adding modest drops to the cycles/iter and raising our BRAM usage). This strategy was not working. Therefore, we decided to evaluate which squares were actually needed. Since there weren't any values larger than 100 (so no more than 10000), we reasoned that as long as we had up to 10001, we would have enough. Sure enough, this did not cause issues with the 10x10 pixel grid and this drastically rudced our cycles/iter, runtime, and BRAM usage. While this solution did not scaled to 100x100 pixels, it gave us insight into the leading sources of waste in our system.

Here is our testing data:

Variables |   |   |   |   | Overall Data |   |   |   |   | Important Data Components |   |   |   |  
-- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | --
**Parallelization of Row Loop** | **Parallelization of Column Loop** | **Paralleization of Square Root Loading** | **Times Output Data is Saved** | **SRAM Square Load Chunks** | **Total Cycles per Accel Block** | **Runtime** | **BRAM** | **Reg** | **Ops** | **SRAM Load Square Map** | **Cycles/iter of Square Root Loading** | **Cycles/iter of Row Foreach** | **Cycles/iter of Column Foreach** | **Out Store Iterations**
5 | 1 | 1 | Every loop | All | 4814846 | 3 minutes, 40 seconds | 28202352 | 288 | 972 | 3309613 | 65541 | 229569 | 114781 | 1200801
5 | 1 | 1 | At the end | All | 3628860 | 3 minutes, 11 seconds | 27203952 | 288 | 1015 | 3309613 | 65541 | 229283 | 114638 | 15101
10 | 1 | 1 | At the end | All | 3514219 | 3 minutes, 55 seconds | 48177072 | 288 | 1292 | 3309613 | 65541 | 114642 | 114638 | 15101
10 | 10 | 1 | At the end | All | KILLED | KILLED | KILLED | KILLED | KILLED |   |   |   |   |  
10 | 2 | 1 | At the end | All | 3456909 | 14 minutes, 13 seconds | 90123312 | 288 | 2533 | 3309613 | 65541 | 57332 | 57328 | 15101
10 | 1 | 1 | At the end | Blocks of 32, no parallelization | 3549023 | 4 minutes, 1 second | 43986864 | 288 | 1295 | 2409961 | - | 114642 | 114638 | 15101
10 | 1 | 1 | At the end | Blocks of 32, parallelized factor of 32 | 3450207 | 30 minutes, 51 seconds | 44161456 | 3264 | 2092 | 3311145 | - | 114642 | 114638 | 15101
10 | 1 | 1 | At the end | All, but lowered number of things loaded | 654192 | 2 minutes, 42 seconds | 9080432 | 288 | 1292 | 505121 | 10006 | 114642 | 114638 | 15101

Note, when we generated our 100x100 pixel image, we used this setup. (We did not get resource utilization data from this test, unfortunately). From this, you see the shortcomings of the scaling and potential optimization routes for future work.

Variables |   |   |   |   | Overall Data |   |   |   |   | Important Data Components |   |   |   |  
-- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | -- | --
**Parallelization of Row Loop** | **Parallelization of Column Loop** | **Paralleization of Square Root Loading** | **Times Output Data is Saved** | **SRAM Square Load Chunks** | **Total Cycles per Accel Block** | **Runtime** | **SRAM Load Square Map** | **Cycles/iter of Square Root Loading** | **Cycles/iter of Row Foreach** | **Cycles/iter of Column Foreach** | **Out Store Iterations**
10 | 1 | 1 | At the end | All (required up to 65536) | 16684648 | 1 hour, 32 minutes, 24 seconds | 3309613 | 65541 | 11462211 | 1146218 | 1510001

## How did this improve either Performance or Resource utilization (Area)?
By saving data at the end, rather than after every instruction, we improved both the performance and resource utilization.

By parallelizing the row and column loops, we modestly improved the performance (throughput).

By reducing the number of squares loaded, we improved both the performance and resource utilization. However, this did not scale because it does not work at higher pixel counts.

## If this makes tradeoffs between these two metrics, how did you choose a balance point?
We largely focused on improving performance rather than resource utilization because our design is so time intensive to run. However, given the memory issues with synthesizing our design, we possibly needed to consider resource utilization more. Additionally, the two tended to change together with our design. For example, going from 1 to 2 parallelization increased the resource utilization (BRAM). It very modestly dropped the cycles/iter but quadrupled the runtime. In situations like this, we prioritized the runtime over the throughput cycles/iter, which matched the resource utilization trend.

# Code
## How can we access your code?
Here is our code: https://github.com/ecwood/ee109-final-project

## How can we run your final design?
Our final design uses Scalasim, based on the issues described [here](#issues-with-vcs-and-zcu).

From the `ee109-final-project` directory, run
```
./run.sh > onehundred_by_onehundred.txt
```

to generate the data file with the shading data. This will be the shading data for one sphere (as generated by `shader_creator.py`, which creates the binary that is input into the accelerator). Then, run
```
python3 reformat_out_data.py
```
to view the output image. Some sample output images that we created are described in the [Results section](#results).

# Results

Here is the first image we created with the system, using a 10x10 pixel grid:
![image](https://github.com/ecwood/broadway_grosses/assets/36611732/b4bb39ff-c5e8-41c4-b2ec-e1ac27ad8ae1)

Here is the second image we created with the system, using a 50x50 pixel grid:
![image](https://github.com/ecwood/broadway_grosses/assets/36611732/14133e77-8700-46d0-bd53-0e16ece52033)

And finally, after optimizing our code, here's the 100x100 pixel image we made (also using a larger sphere):
![image](https://github.com/ecwood/broadway_grosses/assets/36611732/29443cd5-b00e-4e97-b431-2de034252b72)

Here's a 10x10 version with two spheres:
![image](https://github.com/ecwood/broadway_grosses/assets/36611732/371ae6ac-4d0b-4134-a519-e6c170022570)

Here's a 50x50 version with two spheres:
![image](https://github.com/ecwood/broadway_grosses/assets/36611732/2719b33e-1bd2-434a-9314-2d4b20424583)

# Issues with VCS and ZCU
When I try to run the VCS compilation with the 10x10 matrix on Lagos, I get this error. This running it with 512g of memory in the `.sbtopts` file (`-Xmx512g`)

```
[info] welcome to sbt 1.8.2 (Eclipse Adoptium Java 11.0.22)
[info] loading project definition from /home/wooderi/ee109-final-project/project
[info] loading settings for project ee109-final-project from build.sbt ...
[info] set current project to project1 (in build file:/home/wooderi/ee109-final-project/)
Run VCD: false
Stage Args: 
[warn] Area model file VCS_Area.csv for target VCS was missing expected fields: 
[warn] BRAM
[info] Compiling RegisterOperations to /home/wooderi/ee109-final-project/./gen/VCS/RegisterOperations/
[info] Compiling with arguments: --synth, --instrument, --runtime, --fpga, VCS, -v, --test
[info] Logging RegisterOperations to /home/wooderi/ee109-final-project/./logs/VCS/RegisterOperations/
[info] Running in testbench mode
[warn] libisl appears to be missing!  Please install http://isl.gforge.inria.fr/
[info] emptiness (/home/wooderi/bin/emptiness): Installed Version = 1.2, Required Version = 1.2
[warn] In the last 10 seconds, 6.212 (64.2%) were spent in GC. [Heap: 22.13GB free of 22.69GB, max 512.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 5.361 (58.4%) were spent in GC. [Heap: 202.73GB free of 204.19GB, max 512.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] Error getting model for LUTs of SRAMNew node (SRAMNew_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_LUTs.pmml (/models/resources/SRAMNew_LUTs.pmml)!
[warn] Error getting model for FFs of SRAMNew node (SRAMNew_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_FFs.pmml (/models/resources/SRAMNew_FFs.pmml)!
[warn] Error getting model for RAMB18 of SRAMNew node (SRAMNew_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_RAMB18.pmml (/models/resources/SRAMNew_RAMB18.pmml)!
[warn] Error getting model for RAMB32 of SRAMNew node (SRAMNew_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_RAMB32.pmml (/models/resources/SRAMNew_RAMB32.pmml)!
[warn] No model for LUTs of FixAdd node (FixAdd_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_LUTs.pmml (/models/resources/FixAdd_LUTs.pmml)!
[warn] No model for FFs of FixAdd node (FixAdd_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_FFs.pmml (/models/resources/FixAdd_FFs.pmml)!
[warn] No model for RAMB18 of FixAdd node (FixAdd_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_RAMB18.pmml (/models/resources/FixAdd_RAMB18.pmml)!
[warn] No model for RAMB32 of FixAdd node (FixAdd_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_RAMB32.pmml (/models/resources/FixAdd_RAMB32.pmml)!
[warn] No model for LUTs of FixMul node (FixMul_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_LUTs.pmml (/models/resources/FixMul_LUTs.pmml)!
[warn] No model for FFs of FixMul node (FixMul_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_FFs.pmml (/models/resources/FixMul_FFs.pmml)!
[warn] No model for RAMB18 of FixMul node (FixMul_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_RAMB18.pmml (/models/resources/FixMul_RAMB18.pmml)!
[warn] No model for RAMB32 of FixMul node (FixMul_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_RAMB32.pmml (/models/resources/FixMul_RAMB32.pmml)!
[warn] No model for LUTs of FixMod node (FixMod_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_LUTs.pmml (/models/resources/FixMod_LUTs.pmml)!
[warn] No model for FFs of FixMod node (FixMod_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_FFs.pmml (/models/resources/FixMod_FFs.pmml)!
[warn] No model for RAMB18 of FixMod node (FixMod_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_RAMB18.pmml (/models/resources/FixMod_RAMB18.pmml)!
[warn] No model for RAMB32 of FixMod node (FixMod_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_RAMB32.pmml (/models/resources/FixMod_RAMB32.pmml)!
[info] Banking summary report written to /home/wooderi/ee109-final-project/./gen/VCS/RegisterOperations//banking//decisions_47.html
TODO: un-gut memory allocator
[warn] Error getting model for LUTs of NoImpl node (NoImpl_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/NoImpl_LUTs.pmml (/models/resources/NoImpl_LUTs.pmml)!
[warn] Error getting model for FFs of NoImpl node (NoImpl_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/NoImpl_FFs.pmml (/models/resources/NoImpl_FFs.pmml)!
[info] Completed
[success] Total time: 254.1440 seconds
Backend make in /home/wooderi/ee109-final-project/./logs/VCS/RegisterOperations//make.log
make -e VCD_OFF=1
[info] RegisterOperations:
[info] RegisterOperations
[info] - should compile, run, and verify for backend VCS *** FAILED ***
[info]   utils.Result$MakeError: Non-zero exit code 2.
[info] 
[info] Exception: java.lang.OutOfMemoryError thrown from the UncaughtExceptionHandler in thread "classloader-cache-cleanup-0"
[info] Exception in thread "sbt-progress-report-scheduler" java.lang.OutOfMemoryError: Java heap space
[info] Exception in thread "sbt-bg-threads-1" java.lang.OutOfMemoryError: Java heap space
[info]   ...
[info] Run completed in 8 minutes, 3 seconds.
[info] Total number of tests run: 1
[info] Suites: completed 1, aborted 0
[info] Tests: succeeded 0, failed 1, canceled 0, ignored 0, pending 0
[info] *** 1 TEST FAILED ***
[error] Failed tests:
[error]     RegisterOperations
[error] (Test / testOnly) sbt.TestsFailedException: Tests unsuccessful
[error] Total time: 487 s (08:07), completed Jun 5, 2024, 7:02:38 AM
```

The same error occurs when trying running the ZCU compilation:
```
wooderi@lagos:~/ee109-final-project$ cat output_zcu.txt 
[info] welcome to sbt 1.8.2 (Eclipse Adoptium Java 11.0.22)
[info] loading project definition from /home/wooderi/ee109-final-project/project
[info] loading settings for project ee109-final-project from build.sbt ...
[info] set current project to project1 (in build file:/home/wooderi/ee109-final-project/)
Run VCD: false
Stage Args: 
[warn] Area model file ZCU_Area.csv for target ZCU was missing expected fields: 
[warn] BRAM
[info] Compiling RegisterOperations to /home/wooderi/ee109-final-project/./gen/ZCU/RegisterOperations/
[info] Compiling with arguments: --synth, --insanity, --fpga, ZCU, -v, --test
[info] Logging RegisterOperations to /home/wooderi/ee109-final-project/./logs/ZCU/RegisterOperations/
[info] Running in testbench mode
[warn] libisl appears to be missing!  Please install http://isl.gforge.inria.fr/
[info] emptiness (/home/wooderi/bin/emptiness): Installed Version = 1.2, Required Version = 1.2
[warn] In the last 10 seconds, 6.407 (69.6%) were spent in GC. [Heap: 14.04GB free of 14.59GB, max 512.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 5.736 (63.6%) were spent in GC. [Heap: 42.58GB free of 43.78GB, max 512.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 30 seconds, 5.119 (17.2%) were spent in GC. [Heap: 129.73GB free of 131.34GB, max 512.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] Error getting model for LUTs of SRAMNew node (SRAMNew_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_LUTs.pmml (/models/resources/SRAMNew_LUTs.pmml)!
[warn] Error getting model for FFs of SRAMNew node (SRAMNew_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_FFs.pmml (/models/resources/SRAMNew_FFs.pmml)!
[warn] Error getting model for RAMB18 of SRAMNew node (SRAMNew_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_RAMB18.pmml (/models/resources/SRAMNew_RAMB18.pmml)!
[warn] Error getting model for RAMB32 of SRAMNew node (SRAMNew_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/SRAMNew_RAMB32.pmml (/models/resources/SRAMNew_RAMB32.pmml)!
[warn] No model for LUTs of FixAdd node (FixAdd_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_LUTs.pmml (/models/resources/FixAdd_LUTs.pmml)!
[warn] No model for FFs of FixAdd node (FixAdd_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_FFs.pmml (/models/resources/FixAdd_FFs.pmml)!
[warn] No model for RAMB18 of FixAdd node (FixAdd_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_RAMB18.pmml (/models/resources/FixAdd_RAMB18.pmml)!
[warn] No model for RAMB32 of FixAdd node (FixAdd_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/FixAdd_RAMB32.pmml (/models/resources/FixAdd_RAMB32.pmml)!
[warn] No model for LUTs of FixMul node (FixMul_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_LUTs.pmml (/models/resources/FixMul_LUTs.pmml)!
[warn] No model for FFs of FixMul node (FixMul_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_FFs.pmml (/models/resources/FixMul_FFs.pmml)!
[warn] No model for RAMB18 of FixMul node (FixMul_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_RAMB18.pmml (/models/resources/FixMul_RAMB18.pmml)!
[warn] No model for RAMB32 of FixMul node (FixMul_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMul_RAMB32.pmml (/models/resources/FixMul_RAMB32.pmml)!
[warn] No model for LUTs of FixMod node (FixMod_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_LUTs.pmml (/models/resources/FixMod_LUTs.pmml)!
[warn] No model for FFs of FixMod node (FixMod_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_FFs.pmml (/models/resources/FixMod_FFs.pmml)!
[warn] No model for RAMB18 of FixMod node (FixMod_RAMB18.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_RAMB18.pmml (/models/resources/FixMod_RAMB18.pmml)!
[warn] No model for RAMB32 of FixMod node (FixMod_RAMB32.pmml) at path env(SPATIAL_HOME) + /models/resources/FixMod_RAMB32.pmml (/models/resources/FixMod_RAMB32.pmml)!
[info] Banking summary report written to /home/wooderi/ee109-final-project/./gen/ZCU/RegisterOperations//banking//decisions_40.html
TODO: un-gut memory allocator
[warn] Error getting model for LUTs of NoImpl node (NoImpl_LUTs.pmml) at path env(SPATIAL_HOME) + /models/resources/NoImpl_LUTs.pmml (/models/resources/NoImpl_LUTs.pmml)!
[warn] Error getting model for FFs of NoImpl node (NoImpl_FFs.pmml) at path env(SPATIAL_HOME) + /models/resources/NoImpl_FFs.pmml (/models/resources/NoImpl_FFs.pmml)!
[info] Completed
[success] Total time: 241.4900 seconds
Backend make in /home/wooderi/ee109-final-project/./logs/ZCU/RegisterOperations//make.log
make
wooderi@lagos:~/ee109-final-project$ tail -f /home/wooderi/ee109-final-project/./logs/ZCU/RegisterOperations//make.log
set $CLOCK_FREQ_MHZ to [125]
echo "$(date +%s)"  start.log
sed -i "s/EPRINTF(/fprintf(stderr,/g" zcu.sw-resources/FringeContextZCU.h # Not sure why eprintf randomly crashes zcu
sbt "runMain spatialIP.Instantiator --verilog --testArgs zcu"
[info] welcome to sbt 1.8.2 (Eclipse Adoptium Java 11.0.22)
[info] loading project definition from /home/wooderi/ee109-final-project/gen/ZCU/RegisterOperations/project
[info] loading settings for project registeroperations from build.sbt ...
[info] set current project to spatial-app (in build file:/home/wooderi/ee109-final-project/gen/ZCU/RegisterOperations/)
[info] compiling 1 Scala source to /home/wooderi/ee109-final-project/gen/ZCU/RegisterOperations/target/scala-2.12/classes ...
[info] done compiling
[info] running spatialIP.Instantiator --verilog --testArgs zcu
[info] [0.002] Elaborating design...
[warn] In the last 10 seconds, 5.157 (51.6%) were spent in GC. [Heap: 0.02GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.396 (84.0%) were spent in GC. [Heap: 0.02GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.526 (96.1%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.965 (91.5%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.242 (94.4%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.796 (88.3%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.81 (92.0%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.262 (92.8%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.265 (93.3%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.07 (92.7%) were spent in GC. [Heap: 0.01GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.366 (94.7%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.035 (93.3%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.989 (92.5%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.245 (94.6%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.063 (93.4%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.982 (93.0%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.138 (93.8%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.222 (93.4%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.014 (90.3%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 8.98 (93.5%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.207 (94.7%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.018 (94.1%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.395 (95.1%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.36 (95.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.179 (95.3%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.207 (95.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.163 (91.9%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.324 (95.3%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.121 (95.1%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.095 (91.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.598 (96.8%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.376 (97.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.234 (96.5%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.581 (96.9%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.413 (96.8%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.324 (97.3%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.478 (97.1%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.415 (97.3%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.484 (97.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.618 (97.0%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.322 (93.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.529 (97.5%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.345 (97.5%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.532 (97.5%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.59 (96.8%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.345 (97.4%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.523 (97.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.639 (97.2%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[warn] In the last 10 seconds, 9.292 (93.1%) were spent in GC. [Heap: 0.00GB free of 1.00GB, max 1.00GB] Consider increasing the JVM heap using `-Xmx` or try a different collector, e.g. `-XX:+UseG1GC`, for better performance.
[success] Total time: 558 s (09:18), completed Jun 5, 2024, 7:42:01 AM
mv bigIP.tcl verilog-zcu/
Exception in thread "sbt-bg-threads-1" java.lang.OutOfMemoryError: Java heap space
	at scala.reflect.internal.Symbols$Symbol.info_$eq(Symbols.scala:1564)
	at scala.reflect.internal.Symbols$TypeSymbol.info_$eq(Symbols.scala:3211)
	at scala.reflect.internal.Symbols$Symbol.setInfo(Symbols.scala:1570)
	at scala.reflect.runtime.SymbolLoaders.$anonfun$setAllInfos$1(SymbolLoaders.scala:66)
	at scala.reflect.runtime.SymbolLoaders.setAllInfos(SymbolLoaders.scala:66)
	at scala.reflect.runtime.SymbolLoaders.setAllInfos$(SymbolLoaders.scala:65)
	at scala.reflect.runtime.JavaUniverse.setAllInfos(JavaUniverse.scala:30)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.markAbsent$1(JavaMirrors.scala:623)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.unpickleClass(JavaMirrors.scala:653)
	at scala.reflect.runtime.SymbolLoaders$TopClassCompleter.$anonfun$complete$2(SymbolLoaders.scala:37)
	at scala.reflect.runtime.SymbolLoaders$TopClassCompleter$$Lambda$6134/0x0000000100e44440.apply$mcV$sp(Unknown Source)
	at scala.runtime.java8.JFunction0$mcV$sp.apply(JFunction0$mcV$sp.java:23)
	at scala.reflect.internal.SymbolTable.slowButSafeEnteringPhaseNotLaterThan(SymbolTable.scala:333)
	at scala.reflect.runtime.SymbolLoaders$TopClassCompleter.complete(SymbolLoaders.scala:34)
	at scala.reflect.internal.Symbols$Symbol.completeInfo(Symbols.scala:1551)
	at scala.reflect.internal.Symbols$Symbol.info(Symbols.scala:1514)
	at scala.reflect.runtime.SynchronizedSymbols$SynchronizedSymbol$$anon$7.scala$reflect$runtime$SynchronizedSymbols$SynchronizedSymbol$$super$info(SynchronizedSymbols.scala:203)
	at scala.reflect.runtime.SynchronizedSymbols$SynchronizedSymbol.$anonfun$info$1(SynchronizedSymbols.scala:158)
	at scala.reflect.runtime.SynchronizedSymbols$SynchronizedSymbol.info(SynchronizedSymbols.scala:149)
	at scala.reflect.runtime.SynchronizedSymbols$SynchronizedSymbol.info$(SynchronizedSymbols.scala:158)
	at scala.reflect.runtime.SynchronizedSymbols$SynchronizedSymbol$$anon$7.info(SynchronizedSymbols.scala:203)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.coreLookup$1(JavaMirrors.scala:1038)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.lookupClass$1(JavaMirrors.scala:1044)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.classToScala1(JavaMirrors.scala:1061)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.$anonfun$classToScala$1(JavaMirrors.scala:1026)
	at scala.reflect.runtime.JavaMirrors$JavaMirror$$Lambda$6165/0x0000000100e69040.apply(Unknown Source)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.$anonfun$toScala$1(JavaMirrors.scala:137)
	at scala.reflect.runtime.JavaMirrors$JavaMirror$$Lambda$6164/0x0000000100ccb840.apply(Unknown Source)
	at scala.reflect.runtime.TwoWayCaches$TwoWayCache.$anonfun$toScala$1(TwoWayCaches.scala:50)
	at scala.reflect.runtime.TwoWayCaches$TwoWayCache.toScala(TwoWayCaches.scala:46)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.toScala(JavaMirrors.scala:135)
	at scala.reflect.runtime.JavaMirrors$JavaMirror.classToScala(JavaMirrors.scala:1026)
mv: cannot move 'bigIP.tcl' to 'verilog-zcu/': Not a directory
make: *** [Makefile:39: hw] Error 1
```