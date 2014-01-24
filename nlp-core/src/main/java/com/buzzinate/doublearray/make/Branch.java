package com.buzzinate.doublearray.make;

class Branch {
	/**
	 * status 此字的状态1，继续 2，是个词语但是还可以继续 ,3确定 nature 词语性质 0.未知 . 1是姓 . 2 是职位名称 3
	 * 是数量级的词 . 4 是数字词语 5 是标点
	 */
	Branch[] branches = new Branch[0];
	private char c;
	// 状态
	private byte status = 1;
	// 索引
	private short index = -1;
	//真实的单词
	private String value ;
	// 单独查找出来的对象
	Branch branch = null;

	public Branch add(Branch branch) {
		if ((this.branch = this.get(branch.getC())) != null) {
			switch (branch.getStatus()) {
			case 1:
				if (this.branch.getStatus() == 2) {
					this.branch.setStatus(2);
				}
				if (this.branch.getStatus() == 3) {
					this.branch.setStatus(2);
				}
				break;
			case 2:
				this.branch.setStatus(2);
			case 3:
				if (this.branch.getStatus() == 2) {
					this.branch.setStatus(2);
				}
				if (this.branch.getStatus() == 1) {
					this.branch.setStatus(2);
				}
			}
			return this.branch;
		}
		index++;
		if ((index + 1) > branches.length) {
			branches = java.util.Arrays.copyOf(branches, index + 1);
		}
		branches[index] = branch;
		Arrays.sort(branches);
		return branch;
	}

	public Branch(char c, int status, String value) {
		this.c = c;
		this.status = (byte) status;
		this.value = value ;
	}

	int i = 0;

	public Branch get(char c) {
		int i = Arrays.binarySearch(branches, c);
		if (i > -1) {
			return branches[i];
		}
		return null;
	}

	public boolean contains(char c) {
		if (Arrays.binarySearch(branches, c) > -1) {
			return true;
		} else {
			return false;
		}
	}

	public int compareTo(char c) {
		if (this.c > c) {
			return 1;
		} else if (this.c < c) {
			return -1;
		} else
			return 0;
	}

	public boolean equals(char c) {
		if (this.c == c) {
			return true;
		} else {
			return false;
		}
	}
	
	public String toString(){
		return this.value + "\t" + status;
	}

	@Override
	public int hashCode() {
		return c;
	}

	public byte getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = (byte) status;
	}

	public char getC() {
		return this.c;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setStatus(byte status) {
		this.status = status;
	}	
}