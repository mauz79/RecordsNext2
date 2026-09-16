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
dataGiornata[1] = "August 27 1995" 
dataGiornata[2] = "September 10 1995" 
dataGiornata[3] = "September 17 1995" 
dataGiornata[4] = "September 24 1995" 
dataGiornata[5] = "October 01 1995" 
dataGiornata[6] = "October 15 1995" 
dataGiornata[7] = "October 22 1995" 
dataGiornata[8] = "October 29 1995" 
dataGiornata[9] = "November 05 1995" 
dataGiornata[10] = "November 19 1995" 
dataGiornata[11] = "November 26 1995" 
dataGiornata[12] = "December 03 1995" 
dataGiornata[13] = "December 10 1995" 
dataGiornata[14] = "December 17 1995" 
dataGiornata[15] = "December 23 1995" 
dataGiornata[16] = "January 07 1996" 
dataGiornata[17] = "January 14 1996" 
dataGiornata[18] = "January 21 1996" 
dataGiornata[19] = "January 28 1996" 
dataGiornata[20] = "February 04 1996" 
dataGiornata[21] = "February 11 1996" 
dataGiornata[22] = "February 18 1996" 
dataGiornata[23] = "February 25 1996" 
dataGiornata[24] = "March 03 1996" 
dataGiornata[25] = "March 10 1996" 
dataGiornata[26] = "April 10 1996" 
dataGiornata[27] = "March 24 1996" 
dataGiornata[28] = "March 31 1996" 
dataGiornata[29] = "April 06 1996" 
dataGiornata[30] = "April 14 1996" 
dataGiornata[31] = "April 20 1996" 
dataGiornata[32] = "April 28 1996" 
dataGiornata[33] = "May 05 1996" 
dataGiornata[34] = "May 12 1996" 
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
	if (Year < 1995)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
