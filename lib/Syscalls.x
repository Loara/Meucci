modulo Syscalls depends Pointers{
	pt ci;
	static{
		ci=null;
	}
	long Pwrite(int fd, pt buf, ulong size){
		%a{
	mov	eax,1
	mov	edi,[rbp+32]
	mov	rsi,[rbp+24]
	mov	rdx,[rbp+16]
	syscall
		}
	}
	long Pread(int fd, pt buf, ulong size){
		%a{
	mov	eax,0
	mov	edi,[rbp+32]
	mov	rsi,[rbp+24]
	mov	rdx,[rbp+16]
	syscall
		}
	}
	char getChar(){
		char value;
		%a{
	mov	eax,0
	mov	edi,_stdin
	lea	rsi,[rbp-1]
	mov	edx,1
	syscall
		}
		return value;
	}
	void putChar(char c){
		%a{
	mov	eax,1
	mov	edi,_stdout
	lea	rsi,[rbp+16]
	mov	edx,1
	syscall
		}
	}
	void exit(){

		%a{
	mov	rax,60
	xor	rdi,rdi
	syscall
		}

	}
	
	void exit(int ecode){
		%a{
	mov	rax,60
	mov	edi,[rbp+16]
	syscall
		}
	}
	pt brk(pt i){
		%a{
	mov	rax,12
	mov	rdi,[rbp+16]
	syscall
		}
	}
	pt sbrk(long i){
		if(ci==null)
			ci=brk(null);
		pt ul=ci;
		ci=brk(ci+i);
		return ul;
	}
	pt currentIndex(){
		if(ci==null)
			ci=brk(null);
		return ci;
	}
}
