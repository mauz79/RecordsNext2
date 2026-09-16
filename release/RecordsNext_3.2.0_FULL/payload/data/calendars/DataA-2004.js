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
dataGiornata[1] = "September 12 2004" 
dataGiornata[2] = "September 19 2004" 
dataGiornata[3] = "September 22 2004" 
dataGiornata[4] = "September 26 2004" 
dataGiornata[5] = "October 03 2004" 
dataGiornata[6] = "October 17 2004" 
dataGiornata[7] = "October 24 2004" 
dataGiornata[8] = "October 27 2004" 
dataGiornata[9] = "October 31 2004" 
dataGiornata[10] = "November 07 2004" 
dataGiornata[11] = "November 10 2004" 
dataGiornata[12] = "November 14 2004" 
dataGiornata[13] = "November 28 2004" 
dataGiornata[14] = "December 05 2004" 
dataGiornata[15] = "December 12 2004" 
dataGiornata[16] = "December 19 2004" 
dataGiornata[17] = "January 06 2005" 
dataGiornata[18] = "January 09 2005" 
dataGiornata[19] = "January 16 2005" 
dataGiornata[20] = "January 23 2005" 
dataGiornata[21] = "January 30 2005" 
dataGiornata[22] = "February 02 2005" 
dataGiornata[23] = "February 06 2005" 
dataGiornata[24] = "February 13 2005" 
dataGiornata[25] = "February 20 2005" 
dataGiornata[26] = "February 27 2005" 
dataGiornata[27] = "March 06 2005" 
dataGiornata[28] = "March 13 2005" 
dataGiornata[29] = "March 20 2005" 
dataGiornata[30] = "April 10 2005" 
dataGiornata[31] = "April 17 2005" 
dataGiornata[32] = "April 20 2005" 
dataGiornata[33] = "April 24 2005" 
dataGiornata[34] = "May 01 2005" 
dataGiornata[35] = "May 08 2005" 
dataGiornata[36] = "May 15 2005" 
dataGiornata[37] = "May 22 2005" 
dataGiornata[38] = "May 29 2005"
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
