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
dataGiornata[1] = "September 10 2006" 
dataGiornata[2] = "September 17 2006" 
dataGiornata[3] = "September 20 2006" 
dataGiornata[4] = "September 24 2006" 
dataGiornata[5] = "October 01 2006" 
dataGiornata[6] = "October 15 2006" 
dataGiornata[7] = "October 22 2006" 
dataGiornata[8] = "October 25 2006" 
dataGiornata[9] = "October 29 2006" 
dataGiornata[10] = "November 05 2006" 
dataGiornata[11] = "November 12 2006" 
dataGiornata[12] = "November 19 2006" 
dataGiornata[13] = "November 26 2006" 
dataGiornata[14] = "December 03 2006" 
dataGiornata[15] = "December 10 2006" 
dataGiornata[16] = "December 17 2006" 
dataGiornata[17] = "December 20 2006" 
dataGiornata[18] = "December 23 2006" 
dataGiornata[19] = "January 14 2007" 
dataGiornata[20] = "January 21 2007" 
dataGiornata[21] = "January 28 2007" 
dataGiornata[22] = "February 04 2007" 
dataGiornata[23] = "February 11 2007" 
dataGiornata[24] = "February 18 2007" 
dataGiornata[25] = "February 25 2007" 
dataGiornata[26] = "February 28 2007" 
dataGiornata[27] = "March 04 2007" 
dataGiornata[28] = "March 11 2007" 
dataGiornata[29] = "March 18 2007" 
dataGiornata[30] = "April 01 2007" 
dataGiornata[31] = "April 07 2007" 
dataGiornata[32] = "April 15 2007" 
dataGiornata[33] = "April 22 2007" 
dataGiornata[34] = "April 29 2007" 
dataGiornata[35] = "May 06 2007" 
dataGiornata[36] = "May 13 2007" 
dataGiornata[37] = "May 20 2007" 
dataGiornata[38] = "May 27 2007"
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
