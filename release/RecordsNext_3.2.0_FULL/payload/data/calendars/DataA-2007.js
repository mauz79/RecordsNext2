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
dataGiornata[1] = "August 26 2007" 
dataGiornata[2] = "September 02 2007" 
dataGiornata[3] = "September 16 2007" 
dataGiornata[4] = "September 23 2007" 
dataGiornata[5] = "September 26 2007" 
dataGiornata[6] = "September 30 2007" 
dataGiornata[7] = "October 07 2007" 
dataGiornata[8] = "October 21 2007" 
dataGiornata[9] = "October 28 2007" 
dataGiornata[10] = "October 31 2007" 
dataGiornata[11] = "November 04 2007" 
dataGiornata[12] = "November 11 2007" 
dataGiornata[13] = "November 25 2007" 
dataGiornata[14] = "December 02 2007" 
dataGiornata[15] = "December 09 2007" 
dataGiornata[16] = "December 16 2007" 
dataGiornata[17] = "December 23 2007" 
dataGiornata[18] = "January 13 2008" 
dataGiornata[19] = "January 20 2008" 
dataGiornata[20] = "January 27 2008" 
dataGiornata[21] = "February 03 2008" 
dataGiornata[22] = "February 10 2008" 
dataGiornata[23] = "February 17 2008" 
dataGiornata[24] = "February 24 2008" 
dataGiornata[25] = "February 27 2008" 
dataGiornata[26] = "March 02 2008" 
dataGiornata[27] = "March 09 2008" 
dataGiornata[28] = "March 16 2008" 
dataGiornata[29] = "March 19 2008" 
dataGiornata[30] = "March 22 2008" 
dataGiornata[31] = "March 30 2008" 
dataGiornata[32] = "April 06 2008" 
dataGiornata[33] = "April 13 2008" 
dataGiornata[34] = "April 20 2008" 
dataGiornata[35] = "April 27 2008" 
dataGiornata[36] = "May 04 2008" 
dataGiornata[37] = "May 11 2008" 
dataGiornata[38] = "May 18 2008" 
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
