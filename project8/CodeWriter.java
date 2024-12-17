import java.io.*;


public class CodeWriter {
    
    private static CodeWriter instance; // Instance for Singleton
    private BufferedWriter outPutWriter;
    private String outputFileName;
    private int labelCounter = 0;
    private String currentFunction = "";
    private int returnCounter = 0;


    // Private constructor to prevent external instantiation
    private CodeWriter(String outputFileName) {
        try {
            this.outputFileName = outputFileName;
            this.outPutWriter = new BufferedWriter(new FileWriter(outputFileName));
            writeInit();
        } catch (IOException e) {
            System.err.println("error initializing the output writer: " + e.getMessage());
        } 
    }



    // Static method to get the single instance of the class
    public static CodeWriter getInstance(String outputFileName) {
        if (instance == null) {
            instance = new CodeWriter(outputFileName);
        } else {
            throw new IllegalStateException("CodeWriter instance already initialized with an output file.");
        }
        return instance;
    }

     // Writes the assembly code that effects the bootstrap code
     public void writeInit() throws IOException {
        // Initialize SP to 256
        outPutWriter.write("@256");
        outPutWriter.newLine();
        outPutWriter.write("D=A");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();
        
        // Call Sys.init
        writeCall("Sys.init", 0);
    }

    public void writeLabel(String command) {
        try {
            outPutWriter.write("(" + command + ")");
            outPutWriter.newLine();
        } catch (IOException e) {
            System.err.println("Error writing label command: " + e.getMessage());
        }
    }

    public void writeGoto(String command) {
        try {
            outPutWriter.write("@" + command);
            outPutWriter.newLine();
            outPutWriter.write("0;JMP");
            outPutWriter.newLine();
        } catch (IOException e) {
            System.err.println("Error writing label command: " + e.getMessage());
        }
    }

    public void writeIf(String command) {
        try {
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("M=M-1");
            outPutWriter.newLine();
            outPutWriter.write("A=M");
            outPutWriter.newLine();
            outPutWriter.write("D=M");
            outPutWriter.newLine();
            outPutWriter.write("@" + command);
            outPutWriter.newLine();
            outPutWriter.write("D;JNE");
            outPutWriter.newLine();

            
        } catch (IOException e) {
            System.err.println("Error writing label command: " + e.getMessage());
        }
    }

    public void writeCall(String functionName, int nArgs) {
        try {
            String returnLabel = functionName + "$ret." + returnCounter++;
            outPutWriter.write("@" + returnLabel);
            outPutWriter.newLine();
            outPutWriter.write("D=A");
            outPutWriter.newLine();
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("A=M");
            outPutWriter.newLine();
            outPutWriter.write("M=D");
            outPutWriter.newLine();
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("M=M+1");
            outPutWriter.newLine();

            pushSegmentPointer("LCL");
            pushSegmentPointer("ARG");
            pushSegmentPointer("THIS");
            pushSegmentPointer("THAT");

            //  ARG: ARG = SP-5-nArgs
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("D=M");
            outPutWriter.newLine();
            outPutWriter.write("@5");
            outPutWriter.newLine();
            outPutWriter.write("D=D-A");
            outPutWriter.newLine();
            outPutWriter.write("@" + nArgs);
            outPutWriter.newLine();
            outPutWriter.write("D=D-A");
            outPutWriter.newLine();
            outPutWriter.write("@ARG");
            outPutWriter.newLine();
            outPutWriter.write("M=D");
            outPutWriter.newLine();

            // Reposition LCL: LCL = SP
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("D=M");
            outPutWriter.newLine();
            outPutWriter.write("@LCL");
            outPutWriter.newLine();
            outPutWriter.write("M=D");
            outPutWriter.newLine();

            // Transfer control
            outPutWriter.write("@" + functionName);
            outPutWriter.newLine();
            outPutWriter.write("0;JMP");
            outPutWriter.newLine();

            // Insert return address label
            outPutWriter.write("(" + returnLabel + ")");
            outPutWriter.newLine();
                
        } catch (IOException e) {
            System.err.println("Error writing label command: " + e.getMessage());
        }
    }


    public void writeFunction(String functionName, int numLocals) throws IOException {
        outPutWriter.write("(" + functionName + ")");
        outPutWriter.newLine();

        // Initialize local variables to 0
        for (int i = 0; i < numLocals; i++) {
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("A=M");
            outPutWriter.newLine();
            outPutWriter.write("M=0");
            outPutWriter.newLine();
            outPutWriter.write("@SP");
            outPutWriter.newLine();
            outPutWriter.write("M=M+1");
            outPutWriter.newLine();
        }
    }

    private void pushSegmentPointer(String segment) throws IOException {
        outPutWriter.write("@" + segment);
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M+1");
        outPutWriter.newLine();
    }



    public void writeReturn() throws IOException {
        String endFrame = "R13"; // temp variable for LCL
        String retAddr = "R14";  // temp variable for return address

        // endFrame = LCL
        outPutWriter.write("@LCL");
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@" + endFrame);
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();

        // retAddr = *(endFrame - 5)
        outPutWriter.write("@5");
        outPutWriter.newLine();
        outPutWriter.write("A=D-A");
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@" + retAddr);
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();

        // *ARG = pop()
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("AM=M-1");
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@ARG");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();

        // SP = ARG + 1
        outPutWriter.write("@ARG");
        outPutWriter.newLine();
        outPutWriter.write("D=M+1");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();

        // Restore caller's state
        restoreCallerSegment("THAT", endFrame, 1);
        restoreCallerSegment("THIS", endFrame, 2);
        restoreCallerSegment("ARG", endFrame, 3);
        restoreCallerSegment("LCL", endFrame, 4);

        // goto retAddr
        outPutWriter.write("@" + retAddr);
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("0;JMP");
        outPutWriter.newLine();
    }


    private void restoreCallerSegment(String location, String endFrame, int offset) throws IOException {
        outPutWriter.write("@" + endFrame);
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@" + offset);
        outPutWriter.newLine();
        outPutWriter.write("A=D-A");
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@" + location);
        outPutWriter.newLine();
        outPutWriter.write("M=D");
        outPutWriter.newLine();
    }


    public void writeArithmetic(String command) {
        try {
            outPutWriter.write("// " + command);
            outPutWriter.newLine();
            if(command.equals("add")){
                addOrSub_Op("+");
            }
            else if (command.equals("sub")) {
                addOrSub_Op("-");
            }
            else if (command.equals("and")) {
                binaryOp("&");
            }
            else if (command.equals("or")) {
                binaryOp("|");
            }
            else if (command.equals("neg")) {
                unaryOp("-");
            }
            else if (command.equals("not")) {
                unaryOp("!");
            }
            else if (command.equals("eq")) {
                compareOp("JEQ");
            }
            else if (command.equals("gt")) {
                compareOp("JGT");
            }
            else if (command.equals("lt")) {
                compareOp("JLT");
            }
            else{
                throw new IllegalArgumentException("Invalid command: " + command);

            }
        } catch (IOException e) {
            System.err.println("Error writing arithmetic command: " + e.getMessage());
        }
    }
    

    private void addOrSub_Op(String operation) throws IOException {
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("AM=M-1");  // SP--, A=SP
        outPutWriter.newLine();
        outPutWriter.write("D=M");   
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("AM=M-1");  // SP--, A=SP
        outPutWriter.newLine();
        if (operation.equals("+")) {
            outPutWriter.write("M=D+M");  //plus
        } else {
            outPutWriter.write("M=M-D");  //minus
        }
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M+1");  // SP++
        outPutWriter.newLine();
    }

    private void binaryOp(String operation) throws IOException {
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M-1");  // SP--
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("D=M"); 
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M-1");  // SP--
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("D=D" + operation + "M");  // and / or
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("M=D");   // Push result
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M+1");  // SP++
        outPutWriter.newLine();
    }
    
    
    private void unaryOp(String operation) throws IOException {
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M-1");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("M=" + operation + "M");  // *SP = (neg or not) *SP
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M+1");
        outPutWriter.newLine();
    }
    
    

    private void compareOp(String jumpCommand) throws IOException {
        String labelTrue = "TRUE_" + labelCounter;
        String labelEnd = "END_" + labelCounter;
        labelCounter++;
    
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M-1");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("D=M");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M-1");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("D=M-D");  // D = *SP - D
        outPutWriter.newLine();
        outPutWriter.write("@" + labelTrue);
        outPutWriter.newLine();
        outPutWriter.write("D;" + jumpCommand);  // Jump if the condition is true
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("M=0");  // false (0)
        outPutWriter.newLine();
        outPutWriter.write("@" + labelEnd);
        outPutWriter.newLine();
        outPutWriter.write("0;JMP");
        outPutWriter.newLine();
        outPutWriter.write("(" + labelTrue + ")");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("A=M");
        outPutWriter.newLine();
        outPutWriter.write("M=-1");  // true (-1)
        outPutWriter.newLine();
        outPutWriter.write("(" + labelEnd + ")");
        outPutWriter.newLine();
        outPutWriter.write("@SP");
        outPutWriter.newLine();
        outPutWriter.write("M=M+1");
        outPutWriter.newLine();
    }

    

    public void writePushPop(Parser.CommandType commandType, String segment, int index) {
        try {
            if (commandType == Parser.CommandType.C_PUSH) {
                outPutWriter.write("// push " + segment + " " + index);
                outPutWriter.newLine();
        
                if ("constant".equals(segment)) {
                    outPutWriter.write("@" + index);
                    outPutWriter.newLine();
                    outPutWriter.write("D=A");
                    outPutWriter.newLine();
                } 
                else if ("local".equals(segment)) {
                    outPutWriter.write("@LCL");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + index);
                    outPutWriter.newLine();
                    outPutWriter.write("A=D+A");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
                else if ("argument".equals(segment)) {
                    outPutWriter.write("@ARG");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + index);
                    outPutWriter.newLine();
                    outPutWriter.write("A=D+A");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
                else if ("this".equals(segment)) {
                    outPutWriter.write("@THIS");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + index);
                    outPutWriter.newLine();
                    outPutWriter.write("A=D+A");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
                else if ("that".equals(segment)) {
                    outPutWriter.write("@THAT");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + index);
                    outPutWriter.newLine();
                    outPutWriter.write("A=D+A");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
                else if (segment.equals("pointer")) {
                    outPutWriter.write("@" + (3 + index)); //Address of this / that
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
                else if (segment.equals("temp")) {
                    outPutWriter.write("@" + (5 + index)); //Temp starts at 5
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
                else if (segment.equals("static")) {
                    // Get just the filename without path and extension
                    String fileName = new File(this.outputFileName).getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    
                    outPutWriter.write("@" + fileName + "." + index);
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                }
    
                // Push D to stack - last lines are same for all
                outPutWriter.write("@SP");
                outPutWriter.newLine();
                outPutWriter.write("A=M");
                outPutWriter.newLine();
                outPutWriter.write("M=D");
                outPutWriter.newLine();
                outPutWriter.write("@SP");
                outPutWriter.newLine();
                outPutWriter.write("M=M+1");
                outPutWriter.newLine();
            } 
            else if (commandType == Parser.CommandType.C_POP) {
                outPutWriter.write("// pop " + segment + " " + index);
                outPutWriter.newLine();
    
                if (segment.equals("pointer")) {
                    outPutWriter.write("@SP");
                    outPutWriter.newLine();
                    outPutWriter.write("AM=M-1");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + (3 + index)); //Address of this / that
                    outPutWriter.newLine();
                    outPutWriter.write("M=D");
                    outPutWriter.newLine();
                }
                else if (segment.equals("temp")) {
                    outPutWriter.write("@SP");
                    outPutWriter.newLine();
                    outPutWriter.write("AM=M-1");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + (5 + index)); // Temp starts at 5
                    outPutWriter.newLine();
                    outPutWriter.write("M=D");
                    outPutWriter.newLine();
                }
                else if (segment.equals("static")) {
                    // Get just the filename without path and extension
                    String fileName = new File(this.outputFileName).getName();
                    fileName = fileName.substring(0, fileName.lastIndexOf('.'));
                    
                    outPutWriter.write("@SP");
                    outPutWriter.newLine();
                    outPutWriter.write("AM=M-1");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + fileName + "." + index);
                    outPutWriter.newLine();
                    outPutWriter.write("M=D");
                    outPutWriter.newLine();
                }
                else {
                    // For cases - local, argument, this, that
                    String baseAddress = segment.toUpperCase();
                    if (segment.equals("local")) {
                         baseAddress = "LCL";
                    } else if (segment.equals("argument")) {
                        baseAddress = "ARG";
                    }
                    
                    outPutWriter.write("@" + baseAddress);
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@" + index);
                    outPutWriter.newLine();
                    outPutWriter.write("D=D+A");
                    outPutWriter.newLine();
                    outPutWriter.write("@R13");
                    outPutWriter.newLine();
                    outPutWriter.write("M=D");
                    outPutWriter.newLine();
                    outPutWriter.write("@SP");
                    outPutWriter.newLine();
                    outPutWriter.write("AM=M-1");
                    outPutWriter.newLine();
                    outPutWriter.write("D=M");
                    outPutWriter.newLine();
                    outPutWriter.write("@R13");
                    outPutWriter.newLine();
                    outPutWriter.write("A=M");
                    outPutWriter.newLine();
                    outPutWriter.write("M=D");
                    outPutWriter.newLine();
                }
            }
        } catch (IOException e) {
            System.err.println("Error writing push/pop command: " + e.getMessage());
        }
    }

    public void write(String text) throws IOException {
        outPutWriter.write(text);
    }
    
    // close the output writer
    public void close() {
        if (outPutWriter != null) {
            try {
                outPutWriter.close();
            } catch (IOException e) {
                System.err.println("could not close the output stream: " + e.getMessage());
            } finally {
                instance = null;
            }
        }
    }



}
