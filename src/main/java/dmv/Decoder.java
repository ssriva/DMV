package dmv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Decoder {
	
	public static Boolean verbose=false;
	public int[] parent;
	
	public int[] ckyDecode(ArrayList<String> sentence, double[][][] pstop, double[][][]pattach) {
		
		int n=sentence.size();
		parent=new int[n];
		int t=NT.tagset.length;
		Set<String> staglist = new HashSet<String>(sentence);
				
		double[][][][] poptimum = new double[t][3][n][n];
		Tuple[][][][] bp = new Tuple[t][3][n][n];
		
		for(int tag=0;tag<t;tag++){
			for(int dir=0; dir<3; dir++){
				for(int i=0;i<n;i++){
					for(int j=0;j<n;j++){
						poptimum[tag][dir][i][j] = Double.NEGATIVE_INFINITY;
						//bp[tag][dir][i][j]= new Tuple();
					}
				}
			}
		}
		

		
		//(i)Base
		for(int j=0;j<n;j++){		
			int tagindex = Arrays.asList(NT.tagset).indexOf(sentence.get(j));

			poptimum[tagindex][0][j][j]=0.0;
			poptimum[tagindex][1][j][j]= pstop[tagindex][0][1] + poptimum[tagindex][0][j][j];
			poptimum[tagindex][2][j][j]= pstop[tagindex][1][1] + poptimum[tagindex][1][j][j];
			if(verbose){
				System.out.println("pinside["+tagindex+"][1]["+j+"]["+j+"]:"+poptimum[tagindex][1][j][j]);
				System.out.println("pinside["+tagindex+"][2]["+j+"]["+j+"]:"+poptimum[tagindex][2][j][j]);
			}
			
			//These backpointers don't point anywhere (null)
			bp[tagindex][0][j][j]=new Tuple(tagindex,0,j,j); bp[tagindex][0][j][j].headindex=j;
			//System.out.println("bp["+tagindex+"][0]["+j+"]["+j+"]:"+bp[tagindex][0][j][j].headindex);
			bp[tagindex][1][j][j]=new Tuple(tagindex,1,j,j); bp[tagindex][1][j][j].headindex=j;
			bp[tagindex][2][j][j]=new Tuple(tagindex,2,j,j); bp[tagindex][2][j][j].headindex=j;	
		}
		
		//(ii)recurrence
		for(int i=1;i<n;i++){
			for(int j=0;j<=(n-1-i);j++){
				int start = j, end = j+i;
				for(String s1:staglist){
					int tag= Arrays.asList(NT.tagset).indexOf(s1);
					
					//Sum over spans and tag2
					for(int k=start;k<end;k++){
						//w spans start to k, a spans k+1 to end
						//compute adj1(k,w): whether the word to the right(a) is adjacent to w
						int adj=0, adj1=0;
						if(Arrays.asList(NT.tagset).indexOf(sentence.get(k))==tag){
							adj=1;
						}
						if(Arrays.asList(NT.tagset).indexOf(sentence.get(k+1))==tag){
							adj1=1;
						}
						for(String s2:staglist){
							int tag2= Arrays.asList(NT.tagset).indexOf(s2);
										
							double old1=poptimum[tag][0][start][end];
							poptimum[tag][0][start][end]=Math.max(poptimum[tag][0][start][end], Math.log1p(-1.0*Math.exp(pstop[tag][0][adj]))+(pattach[tag][0][tag2]+poptimum[tag][0][start][k]+poptimum[tag2][2][k+1][end]));
							if(poptimum[tag][0][start][end]!=old1 || bp[tag][0][start][end]==null){
								bp[tag][0][start][end]=new Tuple(tag,0,start,end);
								bp[tag][0][start][end].bp.add(new Tuple(tag,0,start,k));
								bp[tag][0][start][end].bp.add(new Tuple(tag2,2,k+1,end));
								if(bp[tag][0][start][k]!=null)
								bp[tag][0][start][end].headindex=bp[tag][0][start][k].headindex;
							}
							
							double old2=poptimum[tag][1][start][end];
							poptimum[tag][1][start][end]=Math.max(poptimum[tag][1][start][end], Math.log1p(-1.0*Math.exp(pstop[tag][1][adj1]))+(pattach[tag][1][tag2]+poptimum[tag2][2][start][k]+poptimum[tag][1][k+1][end]));
							if(poptimum[tag][1][start][end]!=old2 || bp[tag][1][start][end]==null){
								bp[tag][1][start][end]=new Tuple(tag,1,start,end);
								bp[tag][1][start][end].bp.add(new Tuple(tag,1,k+1,end));
								bp[tag][1][start][end].bp.add(new Tuple(tag2,2,start,k));
								if(bp[tag][1][k+1][end]!=null)
								bp[tag][1][start][end].headindex=bp[tag][1][k+1][end].headindex;
							}
						}
						
					}
					
					//Complete Half-seal
					int adj2=0;
					if(Arrays.asList(NT.tagset).indexOf(sentence.get(end))==tag){
						adj2=1;
					}
					
					double old=poptimum[tag][1][start][end];
					////pinside[tag][1][start][end]+= pstop[tag][0][adj2]*pinside[tag][0][start][end];
					poptimum[tag][1][start][end]=Math.max(poptimum[tag][1][start][end], pstop[tag][0][adj2]+poptimum[tag][0][start][end]);
					if(poptimum[tag][1][start][end]!=old || bp[tag][1][start][end]==null){
						bp[tag][1][start][end]=new Tuple(tag,1,start,end);
						bp[tag][1][start][end].bp.add(new Tuple(tag,0,start,end));
						//if()
						bp[tag][1][start][end].headindex=bp[tag][0][start][end].headindex;
						
					}
					//System.out.println("bp["+tag+"][0]["+start+"]["+end+"]:"+bp[tag][0][start][end].headindex+" bp:"+bp[tag][0][start][end].bp.size());
					//System.out.println("bp["+tag+"][1]["+start+"]["+end+"]:"+bp[tag][1][start][end].headindex+" bp:"+bp[tag][1][start][end].bp.size());
					
					//Seal
					int adj3=0;
					if(Arrays.asList(NT.tagset).indexOf(sentence.get(start))==tag){
						adj3=1;
					}
					////pinside[tag][2][start][end] = pstop[tag][1][adj3]*pinside[tag][1][start][end];
					poptimum[tag][2][start][end]=Math.max(poptimum[tag][2][start][end], pstop[tag][1][adj3]+poptimum[tag][1][start][end]);
					bp[tag][2][start][end]=new Tuple(tag,2,start,end);
					bp[tag][2][start][end].bp.add(new Tuple(tag,1,start,end));
					bp[tag][2][start][end].headindex=bp[tag][1][start][end].headindex;
					//System.out.println("bp["+tag+"][2]["+start+"]["+end+"]:"+bp[tag][2][start][end].headindex+" bp:"+bp[tag][2][start][end].bp.size());
					
					if(verbose){
						System.out.println("pinside["+tag+"][0]["+start+"]["+end+"]:"+poptimum[tag][0][start][end]);
						System.out.println("pinside["+tag+"][1]["+start+"]["+end+"]:"+poptimum[tag][1][start][end]);
						System.out.println("pinside["+tag+"][2]["+start+"]["+end+"]:"+poptimum[tag][2][start][end]);
					}
				}
			}
		}
		
		//Attach to root
		poptimum[0][1][0][n-1]=Double.NEGATIVE_INFINITY;
		for(String s:staglist){
			int tag= Arrays.asList(NT.tagset).indexOf(s);
			////pinside[0][1][0][n-1]+= pattach[0][1][tag]*pinside[tag][2][0][n-1];
			
			double old=poptimum[0][1][0][n-1];
			poptimum[0][1][0][n-1] = Math.max(poptimum[0][1][0][n-1],pattach[0][1][tag] + poptimum[tag][2][0][n-1]);
			if(old!=poptimum[0][1][0][n-1] || bp[0][1][0][n-1]==null){
				//System.out.println("Here-1");
				bp[0][1][0][n-1]=new Tuple(0,1,0,n-1);
				bp[0][1][0][n-1].bp.add(new Tuple(tag,2,0,n-1));
				bp[0][1][0][n-1].headindex=bp[tag][2][0][n-1].headindex;
				//System.out.println("ROOT bp[0][1][0][n-1].headindex is "+bp[0][1][0][n-1].headindex+" "+bp[0][1][0][n-1].bp.size());
			}
		}
		//Backtrace
		backtrace(bp[0][1][0][n-1],bp);
		return parent;
		
	}
	
	public void backtrace(Tuple t, Tuple[][][][] bp){
		//System.out.println("In backtrace for pos:"+t.getPos()+" head:"+t.getHead()+" start:"+t.getStart()+" end:"+t.getEnd()+" with size bp:"+bp[t.pos][t.head][t.start][t.end].bp.size()+" headindex:"+bp[t.pos][t.head][t.start][t.end].headindex);
		//if(t.bp.size()==0){
		Tuple t1=bp[t.pos][t.head][t.start][t.end];
		if(t1.bp.size()==0){
			//System.out.println("trivial return");
			return;
		}
		
		if(t1.bp.size()>1){
			//System.out.println("Binary");
			Tuple tmp0=bp[t1.bp.get(0).pos][t1.bp.get(0).head][t1.bp.get(0).start][t1.bp.get(0).end];
			Tuple tmp1=bp[t1.bp.get(1).pos][t1.bp.get(1).head][t1.bp.get(1).start][t1.bp.get(1).end];
			//Tuple tmp0=t1.bp.get(0);
			//Tuple tmp1=t1.bp.get(1);
			
			if(t1.headindex!=tmp0.headindex){
				//System.out.println("Here1");
				parent[tmp0.headindex]=t1.headindex+1;
				//System.out.println("parent["+tmp0.headindex+"] to:"+ t1.headindex);
			}
			else{
				//System.out.println("Here2");
				parent[tmp1.headindex]=t1.headindex+1;
				//System.out.println("parent["+tmp1.headindex+"] to:"+ t1.headindex);
			}
			backtrace(t1.bp.get(0),bp);
			backtrace(t1.bp.get(1),bp);
		}else{
			//System.out.println("Unary");
			backtrace(t1.bp.get(0),bp);
		}
		
		return;
	}
	
}
