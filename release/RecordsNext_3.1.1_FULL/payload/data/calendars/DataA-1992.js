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
dataGiornata[1] = "September 06 1992" 
dataGiornata[2] = "September 13 1992" 
dataGiornata[3] = "September 20 1992" 
dataGiornata[4] = "September 27 1992" 
dataGiornata[5] = "October 04 1992" 
dataGiornata[6] = "October 18 1992" 
dataGiornata[7] = "October 25 1992" 
dataGiornata[8] = "November 01 1992" 
dataGiornata[9] = "November 08 1992" 
dataGiornata[10] = "November 22 1992" 
dataGiornata[11] = "November 29 1992" 
dataGiornata[12] = "December 06 1992" 
dataGiornata[13] = "December 13 1992" 
dataGiornata[14] = "January 03 1993" 
dataGiornata[15] = "January 10 1993" 
dataGiornata[16] = "January 17 1993" 
dataGiornata[17] = "January 24 1993" 
dataGiornata[18] = "January 31 1993" 
dataGiornata[19] = "February 07 1993" 
dataGiornata[20] = "February 14 1993" 
dataGiornata[21] = "February 28 1993" 
dataGiornata[22] = "March 07 1993" 
dataGiornata[23] = "March 14 1993" 
dataGiornata[24] = "March 21 1993" 
dataGiornata[25] = "March 28 1993" 
dataGiornata[26] = "April 04 1993" 
dataGiornata[27] = "April 10 1993" 
dataGiornata[28] = "April 18 1993" 
dataGiornata[29] = "April 25 1993" 
dataGiornata[30] = "May 09 1993" 
dataGiornata[31] = "May 16 1993" 
dataGiornata[32] = "May 23 1993" 
dataGiornata[33] = "May 30 1993" 
dataGiornata[34] = "June 06 1993" 
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
	if (Year < 1992)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
