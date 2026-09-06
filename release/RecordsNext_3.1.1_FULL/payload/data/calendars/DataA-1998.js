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
dataGiornata[1] = "September 13 1998" 
dataGiornata[2] = "September 20 1998" 
dataGiornata[3] = "September 27 1998" 
dataGiornata[4] = "October 04 1998" 
dataGiornata[5] = "October 18 1998" 
dataGiornata[6] = "October 25 1998" 
dataGiornata[7] = "November 01 1998" 
dataGiornata[8] = "November 08 1998" 
dataGiornata[9] = "November 15 1998" 
dataGiornata[10] = "November 22 1998" 
dataGiornata[11] = "November 29 1998" 
dataGiornata[12] = "December 06 1998" 
dataGiornata[13] = "December 13 1998" 
dataGiornata[14] = "December 20 1998" 
dataGiornata[15] = "January 06 1999" 
dataGiornata[16] = "January 10 1999" 
dataGiornata[17] = "January 17 1999" 
dataGiornata[18] = "January 24 1999" 
dataGiornata[19] = "January 31 1999" 
dataGiornata[20] = "February 07 1999" 
dataGiornata[21] = "February 14 1999" 
dataGiornata[22] = "February 22 1999" 
dataGiornata[23] = "February 28 1999" 
dataGiornata[24] = "March 07 1999" 
dataGiornata[25] = "March 14 1999" 
dataGiornata[26] = "March 21 1999" 
dataGiornata[27] = "April 03 1999" 
dataGiornata[28] = "April 11 1999" 
dataGiornata[29] = "April 18 1999" 
dataGiornata[30] = "April 25 1999" 
dataGiornata[31] = "May 02 1999" 
dataGiornata[32] = "May 09 1999" 
dataGiornata[33] = "May 16 1999" 
dataGiornata[34] = "May 23 1999" 
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
	if (Year < 1998)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}
