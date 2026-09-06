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
dataGiornata[1] = "August 28 2005" 
dataGiornata[2] = "September 11 2005" 
dataGiornata[3] = "September 18 2005" 
dataGiornata[4] = "September 21 2005" 
dataGiornata[5] = "September 25 2005" 
dataGiornata[6] = "October 02 2005" 
dataGiornata[7] = "October 16 2005" 
dataGiornata[8] = "October 23 2005" 
dataGiornata[9] = "October 26 2005" 
dataGiornata[10] = "October 30 2005" 
dataGiornata[11] = "November 06 2005" 
dataGiornata[12] = "November 20 2005" 
dataGiornata[13] = "November 27 2005" 
dataGiornata[14] = "December 04 2005" 
dataGiornata[15] = "December 11 2005" 
dataGiornata[16] = "December 18 2005" 
dataGiornata[17] = "December 21 2005" 
dataGiornata[18] = "January 08 2006" 
dataGiornata[19] = "January 15 2006" 
dataGiornata[20] = "January 18 2006" 
dataGiornata[21] = "January 22 2006" 
dataGiornata[22] = "January 29 2006" 
dataGiornata[23] = "February 05 2006" 
dataGiornata[24] = "February 08 2006" 
dataGiornata[25] = "February 12 2006" 
dataGiornata[26] = "February 19 2006" 
dataGiornata[27] = "February 26 2006" 
dataGiornata[28] = "March 05 2006" 
dataGiornata[29] = "March 12 2006" 
dataGiornata[30] = "March 19 2006" 
dataGiornata[31] = "March 26 2006" 
dataGiornata[32] = "April 02 2006" 
dataGiornata[33] = "April 09 2006" 
dataGiornata[34] = "April 15 2006" 
dataGiornata[35] = "April 23 2006" 
dataGiornata[36] = "April 30 2006" 
dataGiornata[37] = "May 07 2006" 
dataGiornata[38] = "May 14 2006"
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
