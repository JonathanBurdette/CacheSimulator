import java.io.*;
import java.util.InputMismatchException;
import java.util.Scanner;

//Jonathan Burdette
//cache simulator to generate cache miss statistics

public class CacheSimulator {
	
	private int totalMisses = 0;
	private Row[][] cache;
	private LinkedList[] LRUHierarchy;
	
	//cache set
	private class Row {
		private int tag = 0;
		private boolean lastUsed = false;
		private boolean v = false;
	}
	
	//linked list class for LRU implementation
	private class LinkedList {
		
		private class Node {
			private int data;
			private Node prev;
			private Node next;
			
			private Node(int data) {
				this.data = data;
			}
		}
		
		private Node head;
		private Node tail;
			
		private LinkedList() {
			head = null;
			tail = null;
		}
		
		//adds node to end of list
		private void add(int num) {
			Node n = new Node(num);
			if(head!=null) {
				head.prev = n;
				n.next = head;
				head = n;	
			} else {
				head = n;
			}
		}
		
		//deletes a node whose data is equal to the number passed in
		private boolean delete(int num) {
			Node cur = head;
			while(cur != null) {
				cur = cur.next;
				tail = cur;
			}
			cur = head;		
			while(cur != null) {
				if(cur.data == num) {		
					if(cur.prev == null) {
						head = null;
						head = cur.next;
						head.prev = null;
					} else if(cur.next == null) {
						tail = null;
						tail = cur.prev;
						tail.next = null;
					} else {
						cur.prev.next = cur.next;
						cur.next.prev = cur.prev;
						cur = null;
					}
					return true;
				}
				cur = cur.next;
			}
			head = null;
			return false;
		}
		
		//gets data for the last node in list
		private int getTail() {
			Node cur = head;
			while(cur.next != null) {
				cur = cur.next;
				tail = cur;
			}
			return tail.data;
		}
	}
	
	//creates cache according to number of cache sets
	public void initializeDirectMapCache(int numCacheSets) {
		cache = new Row[numCacheSets][1];
		for(int i=0; i<cache.length; i++) {
			cache[i][0] = new Row();
		}
	}
	
	//creates cache according to number of cache sets
	public void initializeTwoWayCache(int numCacheSets) {
		cache = new Row[numCacheSets/2][2];
		for(int i=0; i<cache.length; i++) {
			cache[i][0] = new Row();
			cache[i][1] = new Row();
		}
	}
	
	//creates cache according to number of cache sets
	public void initializeFourWayCache(int numCacheSets) {
		cache = new Row[numCacheSets/4][4];
		for(int i=0; i<cache.length; i++) {
			cache[i][0] = new Row();
			cache[i][1] = new Row();
			cache[i][2] = new Row();
			cache[i][3] = new Row();
		}
		
		//creates lru order for each cache set
		LRUHierarchy = new LinkedList[numCacheSets/4];
		for(int i=0; i<LRUHierarchy.length; i++) {
			LRUHierarchy[i] = new LinkedList();
			LRUHierarchy[i].add(0);
			LRUHierarchy[i].add(1);
			LRUHierarchy[i].add(2);
			LRUHierarchy[i].add(3);
		}
	}
	
	//simulates a direct mapped cache
	public void directMap(int blockAddress, int numCacheSets) {
		
		int setIndex = (blockAddress % numCacheSets);
		int tagValue = blockAddress;
		
		//if the valid bit is true and tag matches, it's a hit
		if(cache[setIndex][0].v == true && tagValue == cache[setIndex][0].tag) {
			//it's a hit
		} else {
			cache[setIndex][0].tag = tagValue;
			cache[setIndex][0].v = true;
			totalMisses++;
		}
	}
	
	//simulates a two way cache
	public void twoWay(int blockAddress, int numCacheSets) {
		
		int setIndex = (blockAddress % (numCacheSets / 2));
		int tagValue = blockAddress;
		
		//if the valid bit is true and tag matches, it's a hit
		if(cache[setIndex][0].v == true && tagValue == cache[setIndex][0].tag) {
			cache[setIndex][0].lastUsed = true;
			cache[setIndex][1].lastUsed = false;
		} else if(cache[setIndex][1].v == true && tagValue == cache[setIndex][1].tag) {
			cache[setIndex][1].lastUsed = true;
			cache[setIndex][0].lastUsed = false;
		} else {
			//if the valid bit has not been changed, use that block
			if(cache[setIndex][0].v == false) {
				cache[setIndex][0].tag = tagValue;
				cache[setIndex][0].v = true;
				cache[setIndex][0].lastUsed = true;
				cache[setIndex][1].lastUsed = false;
				totalMisses++;
			} else if(cache[setIndex][1].v == false) {
				cache[setIndex][1].tag = tagValue;
				cache[setIndex][1].v = true;
				cache[setIndex][1].lastUsed = true;
				cache[setIndex][0].lastUsed = false;
				totalMisses++;
			} else {
				//choose block that was not used last for replacement
				if(cache[setIndex][0].lastUsed == false) {
					cache[setIndex][0].tag = tagValue;
					cache[setIndex][0].lastUsed = true;
					cache[setIndex][1].lastUsed = false;
					totalMisses++;
				} else {
					cache[setIndex][1].tag = tagValue;
					cache[setIndex][1].lastUsed = true;
					cache[setIndex][0].lastUsed = false;
					totalMisses++;
				}
			}
		}
	}
	
	//simulates a four way cache
	public void fourWay(int blockAddress, int numCacheSets) {
		
		int setIndex = (blockAddress % (numCacheSets / 4));
		int tagValue = blockAddress;
		
		//if the valid bit is true and tag matches, it's a hit
		if(cache[setIndex][0].v == true && tagValue == cache[setIndex][0].tag) {
			LRUHierarchy[setIndex].delete(0);
			LRUHierarchy[setIndex].add(0);
		} else if(cache[setIndex][1].v == true && tagValue == cache[setIndex][1].tag) {
			LRUHierarchy[setIndex].delete(1);
			LRUHierarchy[setIndex].add(1);
		} else if(cache[setIndex][2].v == true && tagValue == cache[setIndex][2].tag) {
			LRUHierarchy[setIndex].delete(2);
			LRUHierarchy[setIndex].add(2);
		} else if(cache[setIndex][3].v == true && tagValue == cache[setIndex][3].tag) {
			LRUHierarchy[setIndex].delete(3);
			LRUHierarchy[setIndex].add(3);
		} else {
			//if the valid bit has not been changed, use that block
			if(cache[setIndex][0].v == false) {
				cache[setIndex][0].tag = tagValue;
				cache[setIndex][0].v = true;
				LRUHierarchy[setIndex].delete(0);
				LRUHierarchy[setIndex].add(0);
				totalMisses++;
			} else if(cache[setIndex][1].v == false) {
				cache[setIndex][1].tag = tagValue;
				cache[setIndex][1].v = true;
				LRUHierarchy[setIndex].delete(1);
				LRUHierarchy[setIndex].add(1);
				totalMisses++;
			} else if(cache[setIndex][2].v == false) {
				cache[setIndex][2].tag = tagValue;
				cache[setIndex][2].v = true;
				LRUHierarchy[setIndex].delete(2);
				LRUHierarchy[setIndex].add(2);
				totalMisses++;
			} else if(cache[setIndex][3].v == false) {
				cache[setIndex][3].tag = tagValue;
				cache[setIndex][3].v = true;
				LRUHierarchy[setIndex].delete(3);
				LRUHierarchy[setIndex].add(3);
				totalMisses++;
			} else {
				//the last node in the lru linked list is the least used and will be replaced
				if(LRUHierarchy[setIndex].getTail() == 0) {
					cache[setIndex][0].tag = tagValue;
					LRUHierarchy[setIndex].delete(0);
					LRUHierarchy[setIndex].add(0);
					totalMisses++;
				} else if(LRUHierarchy[setIndex].getTail() == 1) {
					cache[setIndex][1].tag = tagValue;
					LRUHierarchy[setIndex].delete(1);
					LRUHierarchy[setIndex].add(1);
					totalMisses++;
				} else if(LRUHierarchy[setIndex].getTail() == 2) {
					cache[setIndex][2].tag = tagValue;
					LRUHierarchy[setIndex].delete(2);
					LRUHierarchy[setIndex].add(2);
					totalMisses++;
				} else {
					cache[setIndex][3].tag = tagValue;
					LRUHierarchy[setIndex].delete(3);
					LRUHierarchy[setIndex].add(3);
					totalMisses++;
				}
			}
		}
	}

	public static void main(String[] args) {
		
		CacheSimulator cs = new CacheSimulator();
		Scanner keyboard = new Scanner(System.in);
		
		int numCacheSets = 0;
		int setAssociativity = 0;
		int blockSize = 0;
		String fileName = "";
		
		//get cache info from user
		try {	
			System.out.print("Enter number of cache sets (1/32/64/128/256/512): ");
			numCacheSets = keyboard.nextInt();

			System.out.print("Enter set associativity (1/2/4): ");
			setAssociativity = keyboard.nextInt();

			System.out.print("Enter block size: ");
			blockSize = keyboard.nextInt();
			
			System.out.println("Initializing cache...");
			
			System.out.print("Enter the filename to check: ");
			fileName = keyboard.next();
			keyboard.close();
		
			//open file and perform cache simulation based on set associativity
			try {
				BufferedReader br = new BufferedReader(new FileReader(fileName));
				String line = br.readLine();
				
				if(setAssociativity == 1) {
					cs.initializeDirectMapCache(numCacheSets);
					while(line != null) {
						int memAddress = Integer.parseInt(line);
						int blockAddress = Math.floorDiv((Math.floorDiv(memAddress, 4)), blockSize);
						cs.directMap(blockAddress, numCacheSets);					
						line = br.readLine();
					}
					
				} else if(setAssociativity == 2) {
					cs.initializeTwoWayCache(numCacheSets);
					while(line != null) {
						int memAddress = Integer.parseInt(line);
						int blockAddress = Math.floorDiv((Math.floorDiv(memAddress, 4)), blockSize);
						cs.twoWay(blockAddress, numCacheSets);	
						line = br.readLine();
					}
					
				} else {
					cs.initializeFourWayCache(numCacheSets);
					while(line != null) {
						int memAddress = Integer.parseInt(line);
						int blockAddress = Math.floorDiv((Math.floorDiv(memAddress, 4)), blockSize);
						cs.fourWay(blockAddress, numCacheSets);	
						line = br.readLine();
					}
				}
				
				System.out.println("Total misses = "+cs.totalMisses);
				double hitRate = 100 - (cs.totalMisses/10000d);
				System.out.println("Hit rate = "+hitRate+"%");
				br.close();
			
			} catch (IOException e) {
				System.err.println("Error reading file.");
			}
			
		} catch(InputMismatchException e) {
			System.err.print("Invalid input");
		}
	}
}
