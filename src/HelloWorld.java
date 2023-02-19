/**
 * 方式二：实现java.lang.Runnable接口
 */
public class HelloWorld implements Runnable{//步骤 1
    @Override
    public void run() {//步骤 2
		//run方法内为具体的逻辑实现
        System.out.println("create thread by runnable implements " + this.toString() );
    }
    public static void main(String[] args) {
        for (int threadid =1; threadid<10; threadid ++)
        {
    	   new Thread(new HelloWorld()). start();
        }
    }
}

