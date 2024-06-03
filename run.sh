# Write any command you want to run
# TEST_DATA_HOME=$PWD/unit_tests sbt -Dtest.CS217=true "; testOnly LoadCSV"
# TEST_DATA_HOME=$PWD/unit_tests sbt -Dtest.CS217=true "; testOnly LoadCSVMultiple"
# TEST_DATA_HOME=$PWD/unit_tests sbt -Dtest.CS217=true "; testOnly AddRegisters"
TEST_DATA_HOME=$PWD/unit_tests sbt -Dtest.CS217=true "; testOnly RegisterOperations"
# TEST_DATA_HOME=$PWD/unit_tests sbt -Dtest.CS217=true "; testOnly SquareRoot"