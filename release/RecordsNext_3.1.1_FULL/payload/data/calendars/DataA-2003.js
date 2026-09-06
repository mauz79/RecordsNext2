var dataGiornata = new Array()
// Immettere le date nel formato inglese: Mese Giorno Anno
// Gennaio = January
// Febbraio = February
// Marzo = March
// Maprile = April
// Maggio = May
// Giugno = June
// Luglio = July
// Agosto = August
// Settembre = September
// Ottobre = October
// Novembre = November
// Dicembre = December
dataGiornata[1] = "August 31 2003" 
dataGiornata[2] = "September 14 2003" 
dataGiornata[3] = "September 21 2003" 
dataGiornata[4] = "September 28 2003" 
dataGiornata[5] = "October 05 2003" 
dataGiornata[6] = "October 19 2003" 
dataGiornata[7] = "October 26 2003" 
dataGiornata[8] = "November 02 2003" 
dataGiornata[9] = "November 09 2003" 
dataGiornata[10] = "November 23 2003" 
dataGiornata[11] = "November 30 2003" 
dataGiornata[12] = "December 07 2003" 
dataGiornata[13] = "December 14 2003" 
dataGiornata[14] = "December 21 2003" 
dataGiornata[15] = "January 06 2004" 
dataGiornata[16] = "January 11 2004" 
dataGiornata[17] = "January 18 2004" 
dataGiornata[18] = "January 25 2004" 
dataGiornata[19] = "February 01 2004" 
dataGiornata[20] = "February 08 2004" 
dataGiornata[21] = "February 15 2004" 
dataGiornata[22] = "February 22 2004" 
dataGiornata[23] = "February 29 2004" 
dataGiornata[24] = "March 07 2004" 
dataGiornata[25] = "March 14 2004" 
dataGiornata[26] = "March 21 2004" 
dataGiornata[27] = "March 28 2004" 
dataGiornata[28] = "April 04 2004" 
dataGiornata[29] = "April 10 2004" 
dataGiornata[30] = "April 18 2004" 
dataGiornata[31] = "April 25 2004" 
dataGiornata[32] = "May 02 2004" 
dataGiornata[33] = "May 09 2004" 
dataGiornata[34] = "May 16 2004" 
function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
