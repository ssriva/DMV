package dmv;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class DMVLearn {
	
	//Global sufficient statistics for all sentences
	static ArrayList<double[][][][]> c = new ArrayList<double[][][][]>();
	static ArrayList<double[][][][][]> w = new ArrayList<double[][][][][]>();
	
	public static ModelParameters EM(ArrayList<ArrayList<String>> sentences, int maxIter) {

		//Model parameters
		int t=NT.tagset.length;
		double[][][] pstop = new double[t][3][2];
		double[][][] pattach = new double[t][3][t];
		
		//Initialize parameters with a harmonic distribution
		{
			double C=1.0;
			//double c=0.1;
			double[][] tagpairlocnR = new double[t][t];
			double[] taglocnR = new double[t];
			double[][] tagpairlocnL = new double[t][t];
			double[] taglocnL = new double[t];
			double[] stopL = new double[t];
			double[] stopR = new double[t];
			double[] freq = new double[t];
			Arrays.fill(freq, 1e-5);

			for(int snum=0;snum<sentences.size();snum++){
				int n=sentences.get(snum).size();
				for(int index=0;index<n;index++){
					int tag = Arrays.asList(NT.tagset).indexOf(sentences.get(snum).get(index));
					for(int k=index+1;k<n;k++){
						int tag2 = Arrays.asList(NT.tagset).indexOf(sentences.get(snum).get(k));
						tagpairlocnR[tag][tag2]+=1.0/(Math.abs(index-k));
						taglocnR[tag]+=1.0/(Math.abs(index-k));
					}
					
					for(int k=0;k<index;k++){
						int tag2 = Arrays.asList(NT.tagset).indexOf(sentences.get(snum).get(k));
						tagpairlocnL[tag][tag2]+=1.0/(Math.abs(index-k));
						taglocnL[tag]+=1.0/(Math.abs(index-k));
					}
					
					if(index==0){
						stopL[tag]+=1.0;
					}
					if(index==n-1){
						stopR[tag]+=1.0;
					}
					freq[tag]+=1.0;
					
				}
			}
			
			pstop[0][0][0] = Double.NEGATIVE_INFINITY;
			pstop[0][0][1] = Double.NEGATIVE_INFINITY; 
			pstop[0][2][0] = Double.NEGATIVE_INFINITY;
			pstop[0][2][1] = Double.NEGATIVE_INFINITY; 
			pstop[0][1][0] = Double.NEGATIVE_INFINITY;
			pstop[0][1][1] = Double.NEGATIVE_INFINITY; 
			
			for(int tag=1;tag<NT.tagset.length;tag++){
				
				pstop[tag][2][0]= Double.NEGATIVE_INFINITY;
				pstop[tag][2][1]= Double.NEGATIVE_INFINITY;
				//System.out.println("INITIALIZE: stopL["+tag+"] is "+stopL[tag]+" and freq["+tag+"] is "+freq[tag]);
				//System.out.println("INITIALIZE: stopR["+tag+"] is "+stopL[tag]+" and freq["+tag+"] is "+freq[tag]);
				pstop[tag][1][0]= Math.min(Math.log(stopL[tag]/freq[tag]+0.1*Math.random()),0.0);
				//System.out.println("pstop["+tag+"][1][0]:"+pstop[tag][1][0]);
				pstop[tag][1][1]= Math.min(Math.log(stopL[tag]/freq[tag]+0.1*Math.random()),0.0);
				//System.out.println("pstop["+tag+"][1][1]:"+pstop[tag][1][1]);
				pstop[tag][0][0]= Math.min(Math.log(stopR[tag]/freq[tag]+0.1*Math.random()),0.0);
				//System.out.println("pstop["+tag+"][0][0]:"+pstop[tag][0][0]);
				pstop[tag][0][1]= Math.min(Math.log(stopR[tag]/freq[tag]+0.1*Math.random()),0.0);
				//System.out.println("pstop["+tag+"][0][1]:"+pstop[tag][0][1]);
						
				//Root attaches to a none
				pattach[tag][0][0]=Double.NEGATIVE_INFINITY;
				pattach[tag][1][0]=Double.NEGATIVE_INFINITY;
				pattach[tag][2][0]=Double.NEGATIVE_INFINITY;
				
				//All attach to left of root with equal probability
				pattach[0][1][tag]= Math.log(1.0/(NT.tagset.length -1));
				pattach[0][0][tag]= Double.NEGATIVE_INFINITY;
				pattach[0][2][tag]= Double.NEGATIVE_INFINITY;
				
				for(int tag2=1;tag2<NT.tagset.length;tag2++){
					pattach[tag][0][tag2] = Math.min(Math.log((tagpairlocnR[tag][tag2]+C)/(taglocnR[tag]+(1e-5)+NT.tagset.length*C)+0.001*Math.random()),0.0);
					pattach[tag][1][tag2] = Math.min(Math.log((tagpairlocnL[tag][tag2]+C)/(taglocnL[tag]+(1e-5)+NT.tagset.length*C)+0.001*Math.random()),0.0);
					pattach[tag][2][tag2] = Double.NEGATIVE_INFINITY;
					//System.out.println("pattach: "+tagpairlocnR[tag][tag2]+" "+taglocnR[tag]+" "+tagpairlocnL[tag][tag2]+" "+taglocnL[tag]);
					//System.out.println("pattach[tag][0][tag2]:"+pattach[tag][0][tag2]+" pattach[tag][1][tag2]:"+pattach[tag][1][tag2]+"pattach[tag][2][tag2]:"+pattach[tag][2][tag2]+ " where tag, tag2 are "+tag+" "+tag2);
				}
			}
		}
		
		//Run EM
		for(int iter=1;iter<=maxIter;iter++){
			
			System.out.println("Entering iteration "+iter);
			//E-Step: get sufficient stats by running I/O algorithm 
			double likelihood=0;
			for(int i=0;i<sentences.size();i++){
				//System.out.println("Processing sentence "+i);
				SufficientStats sf = InsideOutside.getSufficientStats(sentences.get(i),pstop, pattach);
				c.add(sf.getC());
				w.add(sf.getW());
				likelihood+=sf.getL();
				//System.out.println("Likelihood"+i+": "+sf.getL());
			}
			System.out.println("Likelihood in iteration "+iter+" is:"+ likelihood);
			
			//M-Step: re-estimate model parameters 
			System.out.println("Re-estimating model in iteration "+iter);
			//Accumulate counts
			double[] h_2_L_GE = new double[NT.tagset.length];
			double[] h_1_L_GE = new double[NT.tagset.length];
			double[] h_2_E_GE = new double[NT.tagset.length];
			double[] h_1_E_GE = new double[NT.tagset.length];
			double[] h_1_E_G = new double[NT.tagset.length];
			double[] h_0_E_G = new double[NT.tagset.length];
			double[] h_1_E_E = new double[NT.tagset.length];
			double[] h_0_E_E = new double[NT.tagset.length];
			double[][] W_1 = new double[NT.tagset.length][NT.tagset.length];
			double[][] W_0 = new double[NT.tagset.length][NT.tagset.length];
			double[] C_1 = new double[NT.tagset.length];
			double[] C_0 = new double[NT.tagset.length];

			for(int snum=0;snum<sentences.size();snum++){
				int n=sentences.get(snum).size();
				Set<String> staglist = new HashSet<String>(sentences.get(snum));
				for(int index=0;index<n;index++){
					//if(Arrays.asList(NT.tagset).indexOf(sentences.get(snum).get(index))==tag){
					//ITERATE on i and j here:
					int tag = Arrays.asList(NT.tagset).indexOf(sentences.get(snum).get(index));

					for(int k1=0;k1<n;k1++){
						for(int k2=0;k2<=(n-1-k1);k2++){
							int i = k2, j = k2+k1;

							if(i<index && j>=index){
								//if(c.get(snum)[tag][2][i][j]+c.get(snum)[tag][1][i][j]>0)
								//System.out.println("In here1 with "+c.get(snum)[tag][2][i][j]+" and "+c.get(snum)[tag][1][i][j]);
								h_2_L_GE[tag] += c.get(snum)[tag][2][i][j];
								h_1_L_GE[tag] += c.get(snum)[tag][1][i][j];
							}

							if(i==index && j>=index){
								//if(c.get(snum)[tag][2][i][j]+c.get(snum)[tag][1][i][j]>0)
								//System.out.println("In here2 with "+c.get(snum)[tag][2][i][j]+" and "+c.get(snum)[tag][1][i][j]);
								h_2_E_GE[tag] += c.get(snum)[tag][2][i][j];
								h_1_E_GE[tag] += c.get(snum)[tag][1][i][j];
							}

							if(i==index && j>index){
								//if(c.get(snum)[tag][1][i][j]+c.get(snum)[tag][0][i][j]>0)
								//System.out.println("In here3 with "+c.get(snum)[tag][1][i][j]+" and "+c.get(snum)[tag][0][i][j]);
								//if(c.get(snum)[tag][1][i][j]>c.get(snum)[tag][0][i][j])
								//	System.out.print("Problemo1:");
								//else
								//	System.out.print("NoProblemo1:");
								//System.out.println("c.get("+snum+")["+tag+"][1]["+i+"]["+j+"]:"+c.get(snum)[tag][1][i][j]+" and c.get("+snum+")["+tag+"][0]["+i+"]["+j+"]:"+c.get(snum)[tag][0][i][j]);
								h_1_E_G[tag] += c.get(snum)[tag][1][i][j];
								h_0_E_G[tag] += c.get(snum)[tag][0][i][j];
							}

							if(i==index && j==index){
								//if(c.get(snum)[tag][1][i][j]+c.get(snum)[tag][0][i][j]>0)
								//System.out.println("In here4 with "+c.get(snum)[tag][1][i][j]+" and "+c.get(snum)[tag][0][i][j]);
								//if(c.get(snum)[tag][1][i][j]>c.get(snum)[tag][0][i][j])
								//	System.out.println("Problemo2");
								//System.out.println("c.get("+snum+")["+tag+"][1]["+i+"]["+j+"]:"+c.get(snum)[tag][1][i][j]+" and c.get("+snum+")["+tag+"][0]["+i+"]["+j+"]:"+c.get(snum)[tag][0][i][j]);
								
								h_1_E_E[tag] += c.get(snum)[tag][1][i][j];
								h_0_E_E[tag] += c.get(snum)[tag][0][i][j];
							}

							if(i<index && j>=index){
								//C_1[tag] += c.get(snum)[tag][1][i][j];
								//C_0[tag] += c.get(snum)[tag][0][i][j];	
								for(String s1:staglist){
									int tag2= Arrays.asList(NT.tagset).indexOf(s1);
									W_1[tag][tag2] += w.get(snum)[tag][1][tag2][i][j];
									W_0[tag][tag2] += w.get(snum)[tag][0][tag2][i][j];
									C_1[tag] += w.get(snum)[tag][1][tag2][i][j];
									C_0[tag] += w.get(snum)[tag][0][tag2][i][j];
								}
							}
						}
					}			
					//}//I floop
				}
			}

			//Root?
			
			//Reesimate model parameters
			for(int tag=1;tag<NT.tagset.length;tag++){
				//System.out.println(NT.tagset.length);
				//System.out.println("Old pstop[tag][1][0]:"+pstop[tag][1][0]);
				//System.out.println("Old pstop[tag][1][1]:"+pstop[tag][1][1]);
				//System.out.println("Old pstop[tag][0][0]:"+pstop[tag][0][0]);
				//System.out.println("Old pstop[tag][0][1]:"+pstop[tag][0][1]);
				pstop[tag][1][0] = Math.log(h_2_L_GE[tag]/(h_1_L_GE[tag]+(1e-6)));
				pstop[tag][1][1] = Math.log(h_2_E_GE[tag]/(h_1_E_GE[tag]+(1e-6)));
				pstop[tag][0][0] = Math.min(Math.log(h_1_E_G[tag]/(h_0_E_G[tag]+(1e-6))),0.0);
				pstop[tag][0][1] = Math.log(h_1_E_E[tag]/(h_0_E_E[tag]+(1e-6)));
				if(pstop[tag][1][0]>0){System.out.println("Queer1! tag:"+tag+" Num:"+h_2_L_GE[tag]+" Den:"+h_1_L_GE[tag]);}
				//System.out.println("New pstop[tag][1][0]:"+pstop[tag][1][0]); //+" as h_2_L_GE["+tag+"]"+" is "+ h_2_L_GE[tag]+ " and h_1_L_GE[tag] is"+h_1_L_GE[tag]);
				if(pstop[tag][1][1]>0){System.out.println("Queer2! tag:"+tag+" Num:"+h_2_E_GE[tag]+" Den:"+h_1_E_GE[tag]);}
				//System.out.println("New pstop[tag][1][1]:"+pstop[tag][1][1]);
				if(pstop[tag][0][0]>0){System.out.println("Queer3! tag:"+tag+" Num:"+h_1_E_G[tag]+" Den:"+h_0_E_G[tag]);}
				//System.out.println("New pstop[tag][0][0]:"+pstop[tag][0][0]);
				if(pstop[tag][0][1]>0){System.out.println("Queer4! tag:"+tag+" Num:"+h_1_E_E[tag]+" Den:"+h_0_E_E[tag]);}
				//System.out.println("New pstop[tag][0][1]:"+pstop[tag][0][1]);
				
				for(int tag2=1;tag2<NT.tagset.length;tag2++){
					//System.out.println("Old pattach[tag][1][tag2]:"+pattach[tag][1][tag2]);
					//System.out.println("Old pattach[tag][0][tag2]:"+pattach[tag][0][tag2]);
					pattach[tag][1][tag2] = Math.log(W_1[tag][tag2]/(C_1[tag]+(1e-6)));
					if(pattach[tag][1][tag2]>0){System.out.println("QUEER1!! tag:"+tag+" tag2:"+tag2+" num:"+W_1[tag][tag2]+" den:"+C_1[tag]);}
					pattach[tag][0][tag2] = Math.log(W_0[tag][tag2]/(C_0[tag]+(1e-6)));
					if(pattach[tag][0][tag2]>0){System.out.println("QUEER2!! tag:"+tag+" tag2:"+tag2+" num:"+W_0[tag][tag2]+" den:"+C_0[tag]);}
					//System.out.println("New pattach[tag][1][tag2]:"+pattach[tag][1][tag2]);//+" W_1[tag][tag2]:"+W_1[tag][tag2]+" C_1[tag]:"+C_1[tag]);
					//System.out.println("New pattach[tag][0][tag2]:"+pattach[tag][0][tag2]);//+" W_0[tag][tag2]:"+W_0[tag][tag2]+" C_0[tag]:"+C_0[tag]);
				}
			}
			
			c.clear();
			w.clear();

		}//EM Iterations
		
		ModelParameters m = new ModelParameters(pstop, pattach);
		return m;
	}

}
