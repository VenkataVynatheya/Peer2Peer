import java.io.*;

public class PayLoadData
{
    public Payloadpiece[] pieceData;
    public int bSize;
    public static Logger l;
    PayLoadData()
    {
        double d=(double)Constants.fileSize/Constants.pieceSize;
        this.bSize=(int)Math.ceil(d);
        this.pieceData=new Payloadpiece[this.bSize];
        int i=0;
        while(i<this.bSize)
        {
            this.pieceData[i++]=new Payloadpiece();
        }
    }

    public byte[] encodeData()
    {
        int s=0;
        if(this.bSize%8!=0)
        {
            s+=1;
        }
        s+=this.bSize/8;
        byte[] bArray=new byte[s];
        int temp=0;
        int bindx=0;
        int i=1;
        while(i<=this.bSize)
        {
            int t1=this.pieceData[i-1].hasPiece;
            temp=temp<<1;
            if(t1==1)
            {
                temp++;
            }
            if(i%8==0)
            {
                bArray[bindx]=(byte) temp;
                bindx++;
                temp=0;
            }
            i++;
        }
        i--;
        if(i%8!=0)
        {
            int shift=this.bSize-(this.bSize/8)*8;
            temp<<=(8-shift);
            bArray[bindx]=(byte)temp;
        }
        return bArray;
    }

    public void startPayLoad(String pId, boolean hasFile)
    {
        int i=0;
        if(hasFile)
        {
            while(i<bSize)
            {
                this.pieceData[i].setHasPiece(1);
                this.pieceData[i].setSenderpId(pId);
                i++;
            }

        }
        else
        {
            while(i<bSize)
            {
                this.pieceData[i].setHasPiece(0);
                this.pieceData[i].setSenderpId(pId);
                i++;
            }
        }

    }

    class Payloadpiece{
        public int hasPiece;
        public String senderpId;

        public void setHasPiece(int piece) {
            this.hasPiece = piece;
        }
        public void setSenderpId(String senderpId) {
            this.senderpId = senderpId;
        }

    }
}