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
dataGiornata[1] = "October 01 2000" 
dataGiornata[2] = "October 15 2000" 
dataGiornata[3] = "October 22 2000" 
dataGiornata[4] = "November 01 2000" 
dataGiornata[5] = "November 05 2000" 
dataGiornata[6] = "November 12 2000" 
dataGiornata[7] = "November 19 2000" 
dataGiornata[8] = "November 26 2000" 
dataGiornata[9] = "December 03 2000" 
dataGiornata[10] = "December 10 2000" 
dataGiornata[11] = "December 17 2000" 
dataGiornata[12] = "December 23 2000" 
dataGiornata[13] = "January 07 2001" 
dataGiornata[14] = "January 14 2001" 
dataGiornata[15] = "January 21 2001" 
dataGiornata[16] = "January 28 2001" 
dataGiornata[17] = "February 04 2001" 
dataGiornata[18] = "February 11 2001" 
dataGiornata[19] = "February 18 2001" 
dataGiornata[20] = "February 25 2001" 
dataGiornata[21] = "March 04 2001" 
dataGiornata[22] = "March 11 2001" 
dataGiornata[23] = "March 18 2001" 
dataGiornata[24] = "April 01 2001" 
dataGiornata[25] = "April 08 2001" 
dataGiornata[26] = "April 14 2001" 
dataGiornata[27] = "April 22 2001" 
dataGiornata[28] = "April 29 2001" 
dataGiornata[29] = "May 06 2001" 
dataGiornata[30] = "May 13 2001" 
dataGiornata[31] = "May 20 2001" 
dataGiornata[32] = "May 27 2001" 
dataGiornata[33] = "June 10 2001" 
dataGiornata[34] = "June 17 2001" 
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
