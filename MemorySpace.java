/**
 * Represents a managed memory space. The memory space manages a list of allocated 
 * memory blocks, and a list free memory blocks. The methods "malloc" and "free" are 
 * used, respectively, for creating new blocks and recycling existing blocks.
 */
public class MemorySpace {
	
	// A list of the memory blocks that are presently allocated
	private LinkedList allocatedList;

	// A list of memory blocks that are presently free
	private LinkedList freeList;

	/**
	 * Constructs a new managed memory space of a given maximal size.
	 * 
	 * @param maxSize
	 *            the size of the memory space to be managed
	 */
	public MemorySpace(int maxSize) {
		// initiallizes an empty list of allocated blocks.
		allocatedList = new LinkedList();
	    // Initializes a free list containing a single block which represents
	    // the entire memory. The base address of this single initial block is
	    // zero, and its length is the given memory size.
		freeList = new LinkedList();
		freeList.addLast(new MemoryBlock(0, maxSize));
	}

	/**
	 * Allocates a memory block of a requested length (in words). Returns the
	 * base address of the allocated block, or -1 if unable to allocate.
	 * 
	 * This implementation scans the freeList, looking for the first free memory block 
	 * whose length equals at least the given length. If such a block is found, the method 
	 * performs the following operations:
	 * 
	 * (1) A new memory block is constructed. The base address of the new block is set to
	 * the base address of the found free block. The length of the new block is set to the value 
	 * of the method's length parameter.
	 * 
	 * (2) The new memory block is appended to the end of the allocatedList.
	 * 
	 * (3) The base address and the length of the found free block are updated, to reflect the allocation.
	 * For example, suppose that the requested block length is 17, and suppose that the base
	 * address and length of the the found free block are 250 and 20, respectively.
	 * In such a case, the base address and length of of the allocated block
	 * are set to 250 and 17, respectively, and the base address and length
	 * of the found free block are set to 267 and 3, respectively.
	 * 
	 * (4) The new memory block is returned.
	 * 
	 * If the length of the found block is exactly the same as the requested length, 
	 * then the found block is removed from the freeList and appended to the allocatedList.
	 * 
	 * @param length
	 *        the length (in words) of the memory block that has to be allocated
	 * @return the base address of the allocated block, or -1 if unable to allocate
	 */
	public int malloc(int length) {
		if (length <= 0) {
			throw new IllegalArgumentException("Block length must be greater than 0.");
		}

		for (int i = 0; i < freeList.getSize(); i++) {
			MemoryBlock freeBlock = freeList.getBlock(i);

			if (freeBlock.length >= length) {
				// Create the allocated block
				MemoryBlock allocatedBlock = new MemoryBlock(freeBlock.baseAddress, length);
				allocatedList.addLast(allocatedBlock);

				if (freeBlock.length == length) {
					// Exact match: remove the free block entirely
					freeList.remove(i);
				} else {
					// Adjust free block's baseAddress and length
					freeBlock.baseAddress += length;
					freeBlock.length -= length;
				}

				return allocatedBlock.baseAddress;
			}
		}

		// No suitable block found: attempt defragmentation and retry
		defrag();

		// Retry after defragmentation
		for (int i = 0; i < freeList.getSize(); i++) {
			MemoryBlock freeBlock = freeList.getBlock(i);

			if (freeBlock.length >= length) {
				MemoryBlock allocatedBlock = new MemoryBlock(freeBlock.baseAddress, length);
				allocatedList.addLast(allocatedBlock);

				if (freeBlock.length == length) {
					freeList.remove(i);
				} else {
					freeBlock.baseAddress += length;
					freeBlock.length -= length;
				}

				return allocatedBlock.baseAddress;
			}
		}

		// Allocation failed even after defragmentation
		return -1;
	}

	


	/**
	 * Frees the memory block whose base address equals the given address.
	 * This implementation deletes the block whose base address equals the given 
	 * address from the allocatedList, and adds it at the end of the free list. 
	 * 
	 * @param baseAddress
	 *            the starting address of the block to freeList
	 */
	public void free(int address) {
		// Locate the block in allocatedList
		MemoryBlock blockToFree = null;
		for (int i = 0; i < allocatedList.getSize(); i++) {
			MemoryBlock allocatedBlock = allocatedList.getBlock(i);
	
			if (allocatedBlock.baseAddress == address) {
				blockToFree = allocatedBlock;
				allocatedList.remove(i);
				break;
			}
		}
	
		if (blockToFree == null) {
			throw new IllegalArgumentException("Block with base address " + address + " not found in allocated list.");
		}
	
		// Add the block to the free list
		insertInFreeList(blockToFree);
	
		// Defragment the free list after adding the block
		defrag();
	}

	private void insertInFreeList(MemoryBlock block) {
		for (int i = 0; i < freeList.getSize(); i++) {
			MemoryBlock freeBlock = freeList.getBlock(i);
			if (block.baseAddress < freeBlock.baseAddress) {
				freeList.add(i, block); // Insert before the current block
				return;
			}
		}
		// If not inserted earlier, add to the end of the list
		freeList.addLast(block);
	}
	/**
	 * Performs defragmantation of this memory space.
	 * Normally, called by malloc, when it fails to find a memory block of the requested size.
	 * In this implementation Malloc does not call defrag.
	 */
	public void defrag() {
		if (freeList.getSize() <= 1) {
			return; // No defragmentation needed for empty or single-block lists
		}
	
		// Create a new list to store defragmented blocks
		LinkedList newFreeList = new LinkedList();
	
		// Traverse the current free list and merge adjacent blocks
		MemoryBlock prev = freeList.getBlock(0);
		for (int i = 1; i < freeList.getSize(); i++) {
			MemoryBlock current = freeList.getBlock(i);
	
			if (prev.baseAddress + prev.length == current.baseAddress) {
				// Merge current block into the previous block
				prev.length += current.length;
			} else {
				// Add the previous block to the new list and update `prev`
				newFreeList.addLast(prev);
				prev = current;
			}
		}
	
		// Add the last processed block to the new list
		newFreeList.addLast(prev);
	
		// Replace the old free list with the new defragmented list
		freeList = newFreeList;
	}

		
}
