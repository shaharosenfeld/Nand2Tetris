import java.io.*;
import java.security.Key;


public class CodeWriter {
    
    private static CodeWriter instance; // Instance for Singleton
    private BufferedWriter outPutWriter;
    private String outputFileName;
    private int labelCounter = 0;
    private String currentFunction = "";
    private int returnCounter = 0;
    private boolean isInit = false;



    // Private constructor to prevent external instantiation
    private CodeWriter(String outputFileName) {
        try {
            this.outputFileName = outputFileName;
            this.outPutWriter = new BufferedWriter(new FileWriter(outputFileName));
        } catch (IOException e) {
            System.err.println("Error initializing the output writer: " + e.getMessage());
        }
    }



    // Static method to get the single instance of the class
    public static CodeWriter getInstance(String outputFileName) {
        if (instance == null) {
            instance = new CodeWriter(outputFileName);
        }
        return instance;
    }

     
    public void writeInit() throws IOException {
        if (!isInit) {
            // Write bootstrap code header
            outPutWriter.write("// Bootstrap code\n");
            
            // Initialize SP to 256
            outPutWriter.write("@256\n");
            outPutWriter.write("D=A\n");
            outPutWriter.write("@SP\n");
            outPutWriter.write("M=D\n");
            
            // Call Sys.init
            writeCall("Sys.init", 0);
            
            isInit = true;
        }
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

    public void writeCall(String functionName, int nArgs) throws IOException {
        String returnLabel = functionName + "$ret." + returnCounter;
        returnCounter++;

        // Push return address
        outPutWriter.write("@" + returnLabel + "\n");
        outPutWriter.write("D=A\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("A=M\n");
        outPutWriter.write("M=D\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("M=M+1\n");

        // Push LCL
        outPutWriter.write("@LCL\n");
        outPutWriter.write("D=M\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("A=M\n");
        outPutWriter.write("M=D\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("M=M+1\n");

        // Push ARG
        outPutWriter.write("@ARG\n");
        outPutWriter.write("D=M\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("A=M\n");
        outPutWriter.write("M=D\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("M=M+1\n");

        // Push THIS
        outPutWriter.write("@THIS\n");
        outPutWriter.write("D=M\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("A=M\n");
        outPutWriter.write("M=D\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("M=M+1\n");

        // Push THAT
        outPutWriter.write("@THAT\n");
        outPutWriter.write("D=M\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("A=M\n");
        outPutWriter.write("M=D\n");
        outPutWriter.write("@SP\n");
        outPutWriter.write("M=M+1\n");

        // ARG = SP - 5 - nArgs
        outPutWriter.write("@SP\n");
        outPutWriter.write("D=M\n");
        outPutWriter.write("@5\n");
        outPutWriter.write("D=D-A\n");
        outPutWriter.write("@" + nArgs + "\n");
        outPutWriter.write("D=D-A\n");
        outPutWriter.write("@ARG\n");
        outPutWriter.write("M=D\n");

        // LCL = SP
        outPutWriter.write("@SP\n");
        outPutWriter.write("D=M\n");
        outPutWriter.write("@LCL\n");
        outPutWriter.write("M=D\n");

        // goto function
        outPutWriter.write("@" + functionName + "\n");
        outPutWriter.write("0;JMP\n");

        // (return-address)
        outPutWriter.write("(" + returnLabel + ")\n");
    }



    public void writeFunction(String functionName, int nVars) throws IOException {
        currentFunction = functionName;
        outPutWriter.write("// function " + functionName + " " + nVars);
        outPutWriter.newLine();
        outPutWriter.write("(" + functionName + ")");
        outPutWriter.newLine();

        // Initialize local variables to 0
        for (int i = 0; i < nVars; i++) {
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
    // FRAME = LCL
    outPutWriter.write("@LCL\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@R13\n");  // R13 = FRAME
    outPutWriter.write("M=D\n");
    
    // RET = *(FRAME-5)
    outPutWriter.write("@5\n");
    outPutWriter.write("A=D-A\n"); // A = FRAME-5
    outPutWriter.write("D=M\n");   // D = *(FRAME-5)
    outPutWriter.write("@R14\n");  // R14 = RET
    outPutWriter.write("M=D\n");
    
    // *ARG = pop()
    outPutWriter.write("@SP\n");
    outPutWriter.write("AM=M-1\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@ARG\n");
    outPutWriter.write("A=M\n");
    outPutWriter.write("M=D\n");
    
    // SP = ARG + 1
    outPutWriter.write("@ARG\n");
    outPutWriter.write("D=M+1\n");
    outPutWriter.write("@SP\n");
    outPutWriter.write("M=D\n");
    
    // THAT = *(FRAME-1)
    outPutWriter.write("@R13\n");
    outPutWriter.write("A=M-1\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@THAT\n");
    outPutWriter.write("M=D\n");
    
    // THIS = *(FRAME-2)
    outPutWriter.write("@R13\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@2\n");
    outPutWriter.write("A=D-A\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@THIS\n");
    outPutWriter.write("M=D\n");
    
    // ARG = *(FRAME-3)
    outPutWriter.write("@R13\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@3\n");
    outPutWriter.write("A=D-A\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@ARG\n");
    outPutWriter.write("M=D\n");
    
    // LCL = *(FRAME-4)
    outPutWriter.write("@R13\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@4\n");
    outPutWriter.write("A=D-A\n");
    outPutWriter.write("D=M\n");
    outPutWriter.write("@LCL\n");
    outPutWriter.write("M=D\n");
    
    // goto RET
    outPutWriter.write("@R14\n");
    outPutWriter.write("A=M\n");
    outPutWriter.write("0;JMP\n");
}



    private void restoreCallerSegment(String location, String endFrame, int offset) throws IOException {
        outPutWriter.write("@R13");
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