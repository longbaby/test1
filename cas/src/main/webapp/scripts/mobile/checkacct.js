function Card2112Check16(num)
{
	var intSum21=0;
	var intSum12=0;
	for (var i=0;i<15;i++)
	{
		var intCardNoDigits=parseInt(num.charAt(i));
		intSum21+=parseInt(intCardNoDigits*((i+1)%2+1)/10)+intCardNoDigits*((i+1)%2+1)%10;
		intSum12+=parseInt(intCardNoDigits*(i%2+1)/10)+intCardNoDigits*(i%2+1)%10;
	}
	var intCardNoLastDigits=parseInt(num.charAt(15));
	if (((intSum21%10-(10-intCardNoLastDigits)%10)==0)||
		((intSum12%10-(10-intCardNoLastDigits)%10)==0))
	{
	return true;
	}
	else
	{
	return false;
	}
}

function Card2121Check15(num)
{
	var even=0;
	var evenSingleSum=0;
	var evenSum=0;
	var oddSum=0;
	var sum=0;
	var checkdigit=0;

	for (var i=0;i<7;i++)
	{
		even=(parseInt(num.charAt(2*i+1)))*2;
		if(even>9){
			even=String(even);
			evenSingleSum=parseInt(even.charAt(0))+parseInt(even.charAt(1));
			evenSum=evenSum+evenSingleSum;
			}
		else{
			evenSum=evenSum+even;
		}

	}
	for(var j=0;j<7;j++)
	{
		oddSum=oddSum+parseInt(num.charAt(2*j));
	}
	sum=oddSum+evenSum;

	if(sum%10==0){
		if(parseInt(num.charAt(14))==0){
			return true;
		}
		else{
			return false;
		}
	}
	else{
		checkdigit=10-sum%10;

		if(parseInt(num.charAt(14))==checkdigit){
			return true;
		}
		else{

			return false;

		}
	}
}
function Card2112Check19(num)
{
	var intSum21=0;
	for (var i=0;i<18;i++)
	{
		var intCardNoDigits=parseInt(num.charAt(17-i));
		intSum21+=parseInt(intCardNoDigits*((i+1)%2+1)/10)+intCardNoDigits*((i+1)%2+1)%10;
	}
	var intCardNoLastDigits=parseInt(num.slice(18,19));
	if ((intSum21%10-(10-intCardNoLastDigits)%10)==0)//
	{
		return true;
	}
	else
	{
		return false;
	}
}
function Card14Check2121(num)
{
	var intSum21=0;
	var intSum12=0;
	for (var i=0;i<13;i++)
	{
		var intCardNoDigits=parseInt(num.charAt(i));
		intSum21+=parseInt(intCardNoDigits*((i+1)%2+1)/10)+intCardNoDigits*((i+1)%2+1)%10;
		intSum12+=parseInt(intCardNoDigits*(i%2+1)/10)+intCardNoDigits*(i%2+1)%10;
	}
	var intCardNoLastDigits=parseInt(num.charAt(13));
	/*
	if (((intSum21%10-(10-intCardNoLastDigits)%10)==0)||
		((intSum12%10-(10-intCardNoLastDigits)%10)==0))
	{
		return true;
	}
	*/
	if ((intSum21%10-(10-intCardNoLastDigits)%10)==0)
	{
		return true;
	}
	else
	{
		return false;
	}
}


function isValidCardAndAcctPublic(cardNum){
	if( isValidCardPublic(cardNum)) {return true; }
	else if(cardNum.length==19&&AcctCheck19(cardNum)){	return true; }
	else{return false;}
}

function isValidCardPublic(cardNum){
	if(cardNum == null || cardNum == "" || cardNum.length <14 || cardNum.length >19)
	{
		return false;
	}
    if(cardNum.length == 19 && Card2112Check19(cardNum)){
        return true;
    }
    else if(cardNum.length == 16 && Card2112Check16(cardNum)){
        return true;
    }
    else if(cardNum.length == 15 && Card2121Check15(cardNum)){
		return true;
    }
    else if(cardNum.length == 14 && Card14Check2121(cardNum)){
		return true;
    }else{
        return false;
    }
}
 function AcctCheck19(num){
	var p = new Array("11","13","17","19","23","29","31","37","41","43","47","53","59","61","67","71","73");
	var sum=0;
	for( var i=0;i<17;i++){
        var num1 =p[i];
        var num2 = num.charAt(i);
		sum += num1*num2;
	}
	sum=97-sum%97;
	var account1=parseInt(sum/10);
	var account2=sum%10;
    if ((account1.toString()!=num.charAt(17))||(account2.toString()!=num.charAt(18))){
      return false;
    }else{
	  return true;
	}
 }
  function isInterCard(acct)
{
	var cardpin=acct.substr(0,6);
	if(acct.length!=16 && acct.length!=15)
			return false;
	for(var i=0;i<acct.length;i++)
	{
		var oneChar=acct.charAt(i);
    	if(oneChar<'0'||oneChar>'9')
		return false;
    }
	if(getcardtype(acct)=="008")
	//if(cardpin=="427030"||cardpin=="427039"||cardpin=="427029"||cardpin=="427028"||cardpin=="530970"||cardpin=="530990"||cardpin=="548259"||cardpin=="438125"||cardpin=="427020"||cardpin=="438126"||cardpin=="427038"||cardpin=="518750"||cardpin=="548751"||cardpin=="427010"||cardpin=="427019"||cardpin=="558360"||cardpin=="518751"||cardpin=="427018"||cardpin=="518727"||cardpin=="402791"||cardpin=="402792"||cardpin=="427062"||cardpin=="427064"||cardpin=="370246"||cardpin=="370247"||cardpin=="524047"||cardpin=="510529"||cardpin=="439061"||cardpin=="439060")
	{
		var cardbinstr= parent.parent.topFrame.cardBinList.SupportCardBin(9);
		var cardbin="|"+parent.parent.topFrame.cardBinList.getCardBin(acct)+"|";
		if(cardbinstr.indexOf(cardbin)!=-1){
			if (!Card2121Check15(acct)) return false;
			else return true;
		}
		if (!Card2112Check16(acct))
			return false;
		return true;
	}
	return false;
}
function checkacctareaCode(acct,accttype,areaCode)
{
	var cardpin="";
	if(accttype=="003")
	{
		if(acct.length==19){
			if(areaCode==acct.substr(6,4))
				return true;
			else
				return false;
		}else if(acct.length==16){
			if(areaCode==acct.substr(0,4))
				return true;
			else
				return false;
		}
	}
	if(accttype=="X01")
	{
		if(acct.length!=19)
			return false;
		if(areaCode==acct.substr(0,4))
			return true;
		else
			return false;
	}
	if(accttype=="X03")
	{
		if(areaCode==acct.substr(6,4))
			return true;
		else
			return false;
	}
	if(accttype=="009")
	{
		if(acct.length!=19)
			return false;
		if(areaCode==acct.substr(0,4))
			return true;
		else
			return false;
	}
}

function getcardtype(cardnum)
{
	 var get_cardtype_init="nocardbin";
	 var len = 5;
	 for(var j=0;j<cardbinarray.length;j++)
	 {
	     len = cardbinarray[j].length;
			//if(cardbinarray[j]==cardbin)
	     if(cardnum.length>=len && cardnum.substring(0,len)==cardbinarray[j])
	     {
	       return cardtypearray[j];
	     }
	 }
	 return get_cardtype_init;

}

function getcardrule(acct){
	var len;
	for(var j=0;j<cardareabinarray.length;j++)
 	{
		len = cardareabinarray[j].length;
		if(acct.length>=len && acct.substring(0,len)==cardareabinarray[j])
		{
		 return areaRuleNamearray[j];

		}
 	}
 	if(acct.length==16&&Card2112Check16(acct))
 	  return "B";
 	else
 	  return null;
}


function getcardarea(acct){

        var cardrule=getcardrule(acct);
        var get_cardarea;

	if(cardrule==null) return null;
	else if(cardrule=="B"){
		get_cardarea=acct.substring(0,4);
		return get_cardarea;
	}else if(cardrule=="C"){
		get_cardarea=acct.substring(6,10);
		return get_cardarea;
	}else if(cardrule=="U"){
		return 0;
	}else if(cardrule=="Z"){
	       for(var l=0;l<cardunionBranchNoarray.length;l++){

	       	   if(cardunionBranchNoarray[l]==("2"+acct.substring(5,9))){
	       	   	get_cardarea=cardareaarray[l];
	       	   	return get_cardarea;
	       	   }
	       }
	}else if(cardrule=="T"){

		for(var n=0;n<cardunionBranchNoarray.length;n++){

	       	   if(cardunionBranchNoarray[n]==("2"+acct.substring(6,10))){
	       	   	get_cardarea=cardareaarray[n];

	       	   	return get_cardarea;
	       	   }
	       }
	}
}