package bufmgr;

public class Clock extends Replacer{
        
        private int current;
        /**
         * 0 = available
         * 1 = pinned
         * 2 = prevpinned
         * @param bufmgr 
         */
	protected Clock(BufMgr bufmgr) {
		super(bufmgr);
		current = -1;
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
         * 
         * @author sebastian
         * 
         * pickVictim tries to pick a frame which can be replaced.
         * Loops through the frametable upto 2 times looking for a frame to be replaced.
         * For each frame it reaches it checks if its state is available(0), in which the index is returned,
         * or if the state is prevpinned(2), in which case it is set to available.
         * If no index is found in two cycles through the frametable, it means
         * that every frame is unavailable, and -1 is returned
         * 
         * 
         */
	public int pickVictim() {
            int count = 0;
            while (count<frametab.length*2){
                current = (current + 1) % frametab.length;
                
                if (frametab[current].state == 0){
                    return current;
                } else if (frametab[current].state == 2) {
                    frametab[current].state = 0;
                }
                count++;
            }
            return -1;
        }
}
