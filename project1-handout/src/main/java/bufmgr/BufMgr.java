package bufmgr;

import java.util.HashMap;

import global.GlobalConst;
import global.Minibase;
import global.Page;
import global.PageId;

/**
 * <h3>Minibase Buffer Manager</h3> The buffer manager reads disk pages into a
 * main memory page as needed. The collection of main memory pages (called
 * frames) used by the buffer manager for this purpose is called the buffer
 * pool. This is just an array of Page objects. The buffer manager is used by
 * access methods, heap files, and relational operators to read, write,
 * allocate, and de-allocate pages.
 */
@SuppressWarnings("unused")
public class BufMgr implements GlobalConst {
	
	/** Actual pool of pages (can be viewed as an array of byte arrays). */
	protected Page[] bufpool;

	/** Array of descriptors, each containing the pin count, dirty status, etc. */
	protected FrameDesc[] frametab;

	/** Maps current page numbers to frames; used for efficient lookups. */
	protected HashMap<Integer, FrameDesc> pagemap;

	/** The replacement policy to use. */
	protected Replacer replacer;
	
	/**
	 * Constructs a buffer manager with the given settings.
	 * 
	 * @param numbufs: number of pages in the buffer pool
	 */

	public BufMgr(int numbufs) {
	    // initialize the buffer pool and frame table
	    bufpool = new Page[numbufs];
	    frametab = new FrameDesc[numbufs];
	    for (int i = 0; i < numbufs; i++) {
	      bufpool[i] = new Page();
	      frametab[i] = new FrameDesc(i);
	    }
	    
	    // initialize the specialized page map and replacer
	    pagemap = new HashMap<Integer, FrameDesc>(numbufs);
	    replacer = new Clock(this);
	}

	/**
	 * Allocates a set of new pages, and pins the first one in an appropriate
	 * frame in the buffer pool.
	 * 
	 * @param firstpg
	 *            holds the contents of the first page 
	 * @param run_size
	 *            number of new pages to allocate
	 * @return page id of the first new page
	 * @throws IllegalArgumentException
	 *             if PIN_MEMCPY and the page is pinned
	 * @throws IllegalStateException
	 *             if all pages are pinned (i.e. pool exceeded)
	 */
	public PageId newPage(Page firstpg, int run_size) {
            // allocate the run
            PageId firstid = Minibase.DiskManager.allocate_page(run_size);

            // try to pin the first page
            try {pinPage(firstid, firstpg, PIN_MEMCPY);} 
            catch (RuntimeException exc) {
                  // roll back because pin failed
                  for (int i = 0; i < run_size; i++) {
                    firstid.pid += 1;
                    Minibase.DiskManager.deallocate_page(firstid);
                  }
                  // re-throw the exception
                  throw exc;
            }
            // notify the replacer and return the first new page id
            replacer.newPage(pagemap.get(firstid.pid));
            return firstid;
	}

	/**
	 * Deallocates a single page from disk, freeing it from the pool if needed.
	 * Call Minibase.DiskManager.deallocate_page(pageno) to deallocate the page before return.
	 * 
	 * @param pageno
	 *            identifies the page to remove
	 * @throws IllegalArgumentException
	 *             if the page is pinned
	 */
        /*
         * @Author Sebastian
         */
	public void freePage(PageId pageno) throws IllegalArgumentException {
            FrameDesc fdesc = pagemap.get(pageno.pid);
            if( fdesc != null){
                if(fdesc.pincnt>0){
                    throw new IllegalArgumentException();
                }
                pagemap.remove(pageno);
                bufpool[fdesc.index] = new Page();
                frametab[fdesc.index] = new FrameDesc(fdesc.index);
            }
            Minibase.DiskManager.deallocate_page(pageno);
	}

	/**
	 * Pins a disk page into the buffer pool. If the page is already pinned,
	 * this simply increments the pin count. Otherwise, this selects another
	 * page in the pool to replace, flushing the replaced page to disk if 
	 * it is dirty.
	 * 
	 * (If one needs to copy the page from the memory instead of reading from 
	 * the disk, one should set skipRead to PIN_MEMCPY. In this case, the page 
	 * shouldn't be in the buffer pool. Throw an IllegalArgumentException if so. )
	 * 
	 * 
	 * @param pageno
	 *            identifies the page to pin
	 * @param page
	 *            if skipread == PIN_MEMCPY, works as as an input param, holding the contents to be read into the buffer pool
	 *            if skipread == PIN_DISKIO, works as an output param, holding the contents of the pinned page read from the disk
	 * @param skipRead
	 *            PIN_MEMCPY(true) (copy the input page to the buffer pool); PIN_DISKIO(false) (read the page from disk)
	 * @throws IllegalArgumentException
	 *             if PIN_MEMCPY and the page is pinned
	 * @throws IllegalStateException
	 *             if all pages are pinned (i.e. pool exceeded)
	 */
	public void pinPage(PageId pageno, Page page, boolean skipRead) {
		
		throw new UnsupportedOperationException("Not implemented");

	}

	/**
         * @author Kasper Nissen
         * 
	 * Unpins a disk page from the buffer pool, decreasing its pin count.
	 * 
	 * @param pageno
	 *            identifies the page to unpin
	 * @param dirty
	 *            UNPIN_DIRTY if the page was modified, UNPIN_CLEAN otherrwise
	 * @throws IllegalArgumentException
	 *             if the page is not present or not pinned
	 */
	public void unpinPage(PageId pageno, boolean dirty) throws IllegalArgumentException {
		
            if (!pagemap.containsKey(pageno.getPID()))
            {
                throw new IllegalArgumentException();
            }
            
            FrameDesc desc = pagemap.get(pageno.getPID());
            
            if (desc.pincnt == 0)
            {
                throw new IllegalArgumentException();
            }
            
            desc.pincnt--;
            
            if (dirty == UNPIN_DIRTY) {
                desc.dirty = true;
            }
	}

	/**
	 * Immediately writes a page in the buffer pool to disk, if dirty.
	 */
        /*
         * @Author Sebastian
         */
	public void flushPage(PageId pageno) {
            FrameDesc fdesc = pagemap.get(pageno.pid);
            if (fdesc.dirty){
                Minibase.DiskManager.write_page(pageno, bufpool[fdesc.index]);
            }
	}

	/**
	 * Immediately writes all dirty pages in the buffer pool to disk.
	 */
        /*
         * @Author Sebastian
         */
	public void flushAllPages() {
            for(int i = 0; i < frametab.length; i++){
                flushPage(frametab[i].pageno);
            }
	}

	/**
         * @author Kasper Nissen
         * 
	 * Gets the total number of buffer frames.
	 */
	public int getNumBuffers() {
		return frametab.length;
	}

	/**
	 * Gets the total number of unpinned buffer frames.
	 */
        /*
         * @Author Sebastian
         */
	public int getNumUnpinned() {
            int unpin_count = 0;
            for (int i = 0; i < frametab.length; i++){
                if(frametab[i].pincnt==0){
                    unpin_count++;
                }
            }
	return unpin_count;	
	}

} // public class BufMgr implements GlobalConst
