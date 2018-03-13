package bufmgr;

public class Clock extends Replacer{
        
        private int current;
        /**
         * 0 = available
         * 1 = pinned
         * 2 = prepinned
         * @param bufmgr 
         */
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
		fdesc.state=0;
		
	}

	@Override
	public void pinPage(FrameDesc fdesc) {
		fdesc.state=1;
		
	}

	@Override
	public void unpinPage(FrameDesc fdesc) {
		if(fdesc.pincnt == 0){
                    fdesc.state=2;
                }
		
	}

	@Override
        /**
         * Looks for a Frame which is elligable for replacement
         * An elligable frame is a frame which has pincnt 0, but it will not be 
         * replaced unless its state is also 0, if state is not 0, it is 
         * decremented
         * The algorithm uses count to see if an entire cycle of the frametab 
         * has been looked through, and if it has it can be asumed that no 
         * elligable frame is to be found 
         */
	public int pickVictim() {
            int i = 0;
            int findvictim = 1;
            while (findvictim == 1){
                current = (current + 1) % frametab.length;
                if (i>2*frametab.length){
                    return -1;
                }
                if (frametab[current].state == 2)
                    frametab[current].state = 1;
                else if (frametab[current].state == 0){
                    findvictim = 0;
                }
                i++;
            }
            return current;
	}

}
