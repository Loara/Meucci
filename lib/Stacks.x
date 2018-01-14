modulo Stacks depends Memory StdIO{
	type explicit StackEle[typ Y]{
		shadow Y ele;
		shadow StackEle[Y] next;
	}
	costructor shadow iE[typ Y](StackEle[Y] this, Y e, StackEle[y] n){
		this.ele = e;
		this.next = n;
	}
	type Stack[typ o]{
		read uint dim;
		shadow StackEle[o] valv;
		end(){
			StackEle[o] cur = this.valv;
			StackEle[o] next;
			while(cur != null){
				next = cur.next;
				:destroy cur;
				cur = next;
			}
		};
	}
	costructor iniStack[typ T](Stack[T] this){
		this.dim = u0;
		this.valv = null;
	}
	void push[typ T](Stack[T] st, T element){
		declare
			StackEle[T] g;
		enddec
		g = :new iE[T](element, st.valv);
		st.valv = g;
		st.dim =+ u1;
	}
	T pop[typ T](Stack[T] st) errors empty{
		if(st.dim > u0){
			declare
				StackEle[T] g
				T rec;
			enddec
			st.dim =- u1;
			StackEle[T] g = st.valv;
			st.valv = g.next;
			T ret = g.ele;
			:destroy g;
			return ret;
		}
		throw empty;
	}
}
