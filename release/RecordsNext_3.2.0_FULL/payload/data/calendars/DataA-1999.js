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
dataGiornata[1] = "August 29 1999" 
dataGiornata[2] = "September 12 1999" 
dataGiornata[3] = "September 19 1999" 
dataGiornata[4] = "September 26 1999" 
dataGiornata[5] = "October 03 1999" 
dataGiornata[6] = "October 17 1999" 
dataGiornata[7] = "October 24 1999" 
dataGiornata[8] = "October 31 1999" 
dataGiornata[9] = "November 07 1999" 
dataGiornata[10] = "November 21 1999" 
dataGiornata[11] = "November 28 1999" 
dataGiornata[12] = "December 05 1999" 
dataGiornata[13] = "December 12 1999" 
dataGiornata[14] = "December 19 1999" 
dataGiornata[15] = "January 06 2000" 
dataGiornata[16] = "January 09 2000" 
dataGiornata[17] = "January 16 2000" 
dataGiornata[18] = "January 23 2000" 
dataGiornata[19] = "January 30 2000" 
dataGiornata[20] = "February 06 2000" 
dataGiornata[21] = "February 13 2000" 
dataGiornata[22] = "February 20 2000" 
dataGiornata[23] = "February 27 2000" 
dataGiornata[24] = "March 05 2000" 
dataGiornata[25] = "March 12 2000" 
dataGiornata[26] = "March 19 2000" 
dataGiornata[27] = "March 25 2000" 
dataGiornata[28] = "April 02 2000" 
dataGiornata[29] = "April 09 2000" 
dataGiornata[30] = "April 16 2000" 
dataGiornata[31] = "April 22 2000" 
dataGiornata[32] = "April 30 2000" 
dataGiornata[33] = "May 07 2000" 
dataGiornata[34] = "May 14 2000" 
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
	if (Year < 1999)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
