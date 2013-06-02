package com.facebook.android;

import java.util.ArrayList;

public class Clustering {
	static int flag=0;
	static int xmean,ymean;
	static ArrayList<String> phNumber = new ArrayList<String>();
	static ArrayList<Double> xaxis= new ArrayList<Double>();
	static ArrayList<Double> yaxis= new ArrayList<Double>();
	static int cluster_id[]=new int[6];
	static int count=6;
	static ArrayList<ArrayList<Double>> distances;
	static ArrayList<Integer> ignore=new ArrayList<Integer>();
	
	//int counter=0;
	
	public void initialise(){
		int xsum=0,ysum=0;
		for(int i=0;i<xaxis.size();i++){
			cluster_id[i]=i;
			xsum+=xaxis.get(i);
			ysum+=yaxis.get(i);
		}
		xmean=xsum/xaxis.size();
		ymean=ysum/yaxis.size();
	}

	public void input(){
		distances = new ArrayList<ArrayList<Double>>();
		for(int i=0;i<xaxis.size();i++){
			ArrayList<Double> t=new ArrayList<Double>();
			for(int j=0;j<xaxis.size();j++){
				if(i!=j){
					double sum;
					double temp=xaxis.get(i)-xaxis.get(j);
					if(temp>=0)
						sum=temp;
					else
						sum=-(temp);
					temp=yaxis.get(i)-yaxis.get(j);
					if(temp>=0)
						sum+=temp;
					else
						sum+=-(temp);
					t.add(sum);

				}
				else{
					t.add(Double.parseDouble("0"));
				}
			}
			distances.add(t);
		}

	}


	public int[] findmin(){
		int index[]=new int[2];
		Double min=10000000000.0;
		for(int i=0;i<distances.size();i++){
			for(int j=i;j<distances.size();j++){
				if(i!=j && distances.get(i).get(j)<min && ignore.contains(i)==false && ignore.contains(j)==false){
					min=distances.get(i).get(j);
					index[0]=i;
					index[1]=j;
				}
			}
		}
		if(min>((xmean+ymean)))
			flag=1;
		return index;
	}


	public String main(){
		StringBuffer result=new StringBuffer();
		/*xaxis.add(100000.0);
		xaxis.add(999.0);
		xaxis.add(998.0);
		xaxis.add(997.0);
		xaxis.add(996.0);
		yaxis.add(1.0);
		yaxis.add(1.0);
		yaxis.add(1.0);
		yaxis.add(1.0);
		yaxis.add(1.0);*/
		initialise();
		input();
		
		while(flag==0){
			int arr[]=findmin();
			//System.out.println("\n"+arr[0]+" "+arr[1]+" "+count);
			//result.append("\n"+arr[0]+" "+arr[1]+" "+count);
			if(arr[0]==arr[1])
				break;
			double temp=(xaxis.get(arr[0])+xaxis.get(arr[1]))/2;
			double temp1=(yaxis.get(arr[0])+yaxis.get(arr[1]))/2;
			if(arr[0]<arr[1]){
				for(int i=0;i<cluster_id.length;i++)
					if(cluster_id[i]==cluster_id[arr[1]] && i!=arr[1])
						cluster_id[i]=cluster_id[arr[0]];
				cluster_id[arr[1]]=cluster_id[arr[0]];	
				//xaxis.remove(arr[1]);
				//yaxis.remove(arr[1]);
				//xaxis.set(arr[1],(double)0);
				//yaxis.set(arr[1],(double)0);
				ignore.add(arr[1]);
				xaxis.set(arr[0], temp);
				yaxis.set(arr[0], temp1);
			}
			else{
				for(int i=0;i<cluster_id.length;i++)
					if(cluster_id[i]==cluster_id[arr[0]] && i!=arr[0])
						cluster_id[i]=cluster_id[arr[1]];
				cluster_id[arr[0]]=cluster_id[arr[1]];
				//xaxis.remove(arr[0]);
				//yaxis.remove(arr[0]);
				//xaxis.set(arr[1],(double)0);
				//yaxis.set(arr[1],(double)0);
				ignore.add(arr[0]);
				xaxis.set(arr[1], temp);
				yaxis.set(arr[1], temp1);
			}
			input();
			//System.out.println();
			//System.out.println();
			//result.append("\n\n");
			
			for(int i=0; i<xaxis.size();i++){
				//System.out.println(xaxis.get(i));
				//result.append(xaxis.get(i));
				//System.out.println(yaxis.get(i));
			}
			//System.out.println("\nhello"+cluster_id.length);
			for(int i=0;i<cluster_id.length;i++){
				//System.out.println(cluster_id[i]);
				result.append(phNumber.get(i));
				result.append(":");
				result.append(cluster_id[i]);
				result.append("\n");
			}
			result.append("\n\n\n");
		}
		//result.append("sdfdfgd");
		return result.toString();
	}
}