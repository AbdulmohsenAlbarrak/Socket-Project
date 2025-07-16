import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) throws Exception{
        //note: server accepts messages in this aggreed upon format "msg:checksum" 
        int port = 8000; 
        String ipOfServer = "82.167.122.171";
        //try to connect to the server.
        Socket client = null;
        try{
            client = new Socket(ipOfServer, port);
        }catch(Exception e){
            System.out.println("connection failed, please try again later."); // Print error if connection fails
            System.exit(0);
        }
        //connection established !
        System.out.println("Connection successfully established!");

        //read from user
        Scanner inFromUser = new Scanner(System.in);
        //read from server
        BufferedReader input = new BufferedReader(new InputStreamReader(client.getInputStream()));
        //output to server
        DataOutputStream output = new DataOutputStream(client.getOutputStream());
        //randomiser
        SecureRandom random = new SecureRandom();

        String msg = "";
        while(!msg.equals("Quit:")){
            System.out.println("please enter a msg: ");
            msg = inFromUser.nextLine();
            if(msg.length()==0){//check if the message is not valid
                System.out.println("please enter a valid non-empty message");
                continue;
            }
            //check if it's a quit message
            if(msg.equals("Quit")){
                //if it's a quit message, notifiy the server of this message, and stop the client
                output.writeBytes(msg+":"+getChecksum(msg)+"\n");
                System.out.println(input.readLine());//print ack. of the quitting msg.
                break;
            }

            //calculate checksum
            String checkSum = getChecksum(msg);

            //to simulate the errors, this will pick an error probablity by random, than apply it to the msg.
            double[] possibleErrors = {30, 50, 0}; //30%, 50%, 100%
            msg = sabotage(msg, possibleErrors[random.nextInt(3)]);
            //place in proper format (msg+:+checksum)
            msg += ":" + checkSum;
            //send the msg to the server. 
            output.writeBytes(msg+"\n");
            //print the response from the server.
            System.out.println(input.readLine());
        }
        client.close();
        inFromUser.close();
        input.close();
        output.close();

    }
    public static String sabotage(String msg, double percentage){
        SecureRandom random = new SecureRandom();
        int n = random.nextInt(1, 11); //returns a number in this interval [1, 10].
        //if the percentage was 40%=0.4, this is the exact probablity as selecting a number less
        //than or equal to 4 out of 10 numbers (4/10=0.4).
        
        if(n <=percentage/10){
            char sabotageLetter = 'X';
            char firstL = msg.charAt(0);
            if(sabotageLetter == firstL)
                sabotageLetter = 'x';
            
            msg = msg.replace(firstL, sabotageLetter);
        }
        return msg;
    }



    public static String getChecksum(String msg){
        //each character is stored in 2 bytes (16 bits) in java, but the charecters in english are all<= 8 bits, although in this implementation we'd like to 
        //assume that every character (even in other languages) are used. 
        long checkSum =0;
        for(char letter: msg.toCharArray()){
            //this will loop over each letter, summing them up as integers, then converting them to binary.
            checkSum = Long.parseLong(String.valueOf(checkSum), 2);//checksum to integer
            checkSum += letter;//checksum + letter (all in integer)
            checkSum = Long.valueOf(Long.toBinaryString(checkSum));//checksum to binary
            
            //now that we have checksum in binary, this will check if it has carry
            //   xxx1 xxxx xxxx xxxx xxxx->checkSum, AND it with 0x10000, if the answer is 1 then there is a carry, wrap around!
            //   0001 0000 0000 0000 0000 
            if(String.valueOf(checkSum).length()>16){//overflow..
                checkSum = checkSum-10000000000000000L;//remove last digit.
                //re-add the carry:
                checkSum = checkSum+0b1;
            }

        }
        //get one's complement of the binary form
        checkSum = ~checkSum;

        return String.valueOf(checkSum);
    }
}
