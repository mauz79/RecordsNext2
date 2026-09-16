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
dataGiornata[1] = "September 01 1991" 
dataGiornata[2] = "September 08 1991" 
dataGiornata[3] = "September 15 1991" 
dataGiornata[4] = "September 22 1991" 
dataGiornata[5] = "September 29 1991" 
dataGiornata[6] = "October 06 1991" 
dataGiornata[7] = "October 20 1991" 
dataGiornata[8] = "October 27 1991" 
dataGiornata[9] = "November 03 1991" 
dataGiornata[10] = "November 17 1991" 
dataGiornata[11] = "November 24 1991" 
dataGiornata[12] = "December 01 1991" 
dataGiornata[13] = "December 08 1991" 
dataGiornata[14] = "December 15 1991" 
dataGiornata[15] = "January 05 1992" 
dataGiornata[16] = "January 12 1992" 
dataGiornata[17] = "January 19 1992" 
dataGiornata[18] = "January 26 1992" 
dataGiornata[19] = "February 02 1992" 
dataGiornata[20] = "February 09 1992" 
dataGiornata[21] = "February 16 1992" 
dataGiornata[22] = "February 23 1992" 
dataGiornata[23] = "March 01 1992" 
dataGiornata[24] = "March 08 1992" 
dataGiornata[25] = "March 15 1992" 
dataGiornata[26] = "March 29 1992" 
dataGiornata[27] = "April 05 1992" 
dataGiornata[28] = "April 12 1992" 
dataGiornata[29] = "April 18 1992" 
dataGiornata[30] = "April 26 1992" 
dataGiornata[31] = "May 03 1992" 
dataGiornata[32] = "May 10 1992" 
dataGiornata[33] = "May 17 1992" 
dataGiornata[34] = "May 24 1992" 
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
	if (Year < 1991)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
