package bufmgr;

public class Clock extends Replacer{
        
        private int current;
        
	protected Clock(BufMgr bufmgr) {
		super(bufmgr);
		current = 0;
	}

	@Override
	public void newPage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void freePage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void pinPage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void unpinPage(FrameDesc fdesc) {
		// TODO Auto-generated method stub
		
	}

	@Override
        /**
         * Looks for a Frame which is elligable for replacement
         * An elligable frame is a frame which has pincnt 0, but it will not be 
         * replaced unless its state is also 0, if state is not 0, it is 
         * decremented
         * The algorithm uses count to see if an entire cycle of the frametab 
         * has been looked through, and if it has it can be usemed that no 
         * elligable frame is to be found 
         */
	public int pickVictim() {
            int count = 0;
		while(count<frametab.length){
                    if(frametab[current].pincnt == 0){
                        if(frametab[current].state == 0){
                            return current;
                        } else {
                           frametab[current].state--; 
                        }
                    }
                    count++;
                    current= (current+1) % (frametab.length - 1);
                }
		return -1;
	}

}
