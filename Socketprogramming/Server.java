import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class Server{
    public static void main(String[] args) throws Exception{
        //any message format will be: "msg:checksum", where we used the special character ":" to seprate the msg from the checksum
        
        //the port number of the server.
        int port = 8000;
        //create new socket for the server
        ServerSocket server = new ServerSocket(port);
        //wait until you get a client
        Socket connectionSocket = server.accept();
        //connection esablished with the client!
        System.out.println("connection successfully established with the client.");       
        //create the server's input buffer (msg from client)
        BufferedReader input = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
        //create the server's output stream.
        DataOutputStream output = new DataOutputStream(connectionSocket.getOutputStream());
        
        String msg = "";
        while(true){
            //wait until msg arrives. (passivly lestin)

            msg = input.readLine();
            //check if the message is in valid format.
            if(msg.length()==0){
                output.writeBytes("invalid message format.\n");
                continue;
            }
            
            //check if the checksum of the msg is the same as the checksum delevired. 
            int flag = msg.indexOf(":");
            String checkSumInMsg = msg.substring(flag+1);
            String checkSumOfMsg = getChecksum(msg.substring(0, flag));
            if(!checkSumInMsg.equals(checkSumOfMsg)){//faulty msg.
                output.writeBytes("Server: error detected, the recived message is incorrect.\n");
                continue;
            }

            //since the message has no errors, check if it's a Quit message.
            String msgBody = msg.substring(0, flag);
            if(msgBody.equals("Quit")){
                //termniate connection
                System.out.println("Client has terminated the connection.");
                output.writeBytes("Server: terminating the connection.\n");
                break;
            }

            //message is in valid format, and has no errors, send ack.
            output.writeBytes("Server: Message recived.\n");
            System.out.println("Message recived, the msg is: "+msgBody);
        }

        connectionSocket.close();
        server.close();
        input.close();
    }

    
    public static String getChecksum(String msg){
    //each character is stored in 2 bytes (16 bits) in java, but the charecters in english are all<= 8 bits
    //although in this implementation we'd like to assume that every character (even in other languages) can be used. 
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