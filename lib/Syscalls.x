modulo Syscalls depends Pointers{
	pt ci;
	static{
		ci=null;
	}
	uint open(pt name, int flag, int mode){
		%a{
	mov	eax, 2	;Il protocollo di chiamata è cambiato
	mov	rdi,[rbp+16]
	mov	esi,[rbp+24]
	mov	edx,[rbp+32]
	syscall
		}
	}
	int close(int fd){
		%a{
	mov	eax,3
	mov	edi,[rbp+16]
	syscall
		}
	}
	long Pwrite(int fd, pt buf, ulong size){
		%a{
	mov	eax,1
	mov	edi,[rbp+16]
	mov	rsi,[rbp+24]
	mov	rdx,[rbp+32]
	syscall
		}
	}
	long Pread(int fd, pt buf, ulong size){
		%a{
	mov	eax,0
	mov	edi,[rbp+16]
	mov	rsi,[rbp+24]
	mov	rdx,[rbp+32]
	syscall
		}
	}
	char getChar(){
		char value;
		%a{
	mov	eax,0
	mov	edi,0
	lea	rsi,[rbp-1]
	mov	edx,1
	syscall
		}
		return value;
	}
	void putChar(char c){
		%a{
	mov	eax,1
	mov	edi,1
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
	pt mmap(pt addr, ulong size, int prot, int flags, int fd, ulong off){
		%a{
	mov	eax, 9
	mov	rdi, [rbp+16]
	mov	rsi, [rbp+24]
	mov	edx, [rbp+32]
	mov	r10d, [rbp+40]
	mov	r8d, [rbp+48]
	mov	r9, [rbp+56]
	syscall
		}
	}
	pt mmap(ulong size){
		//MAP_PRIVATE 2
		//MAP_ANONYMOUS	0x20
		return mmap(null, size, 3, 34, -1, u0l);
	}
	int munmap(pt addr, ulong size){
		%a{
	mov	eax, 11
	mov	rdi, [rbp+16]
	mov	rsi, [rbp+24]
	syscall
		}
	}
}
