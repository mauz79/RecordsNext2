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
dataGiornata[1] = "august 23 2009" 
dataGiornata[2] = "august 30 2009" 
dataGiornata[3] = "september 13 2009" 
dataGiornata[4] = "september 20 2009" 
dataGiornata[5] = "september 23 2009" 
dataGiornata[6] = "september 27 2009" 
dataGiornata[7] = "october 04 2009" 
dataGiornata[8] = "october 18 2009" 
dataGiornata[9] = "october 25 2009" 
dataGiornata[10] = "october 28 2009" 
dataGiornata[11] = "november 01 2009" 
dataGiornata[12] = "november 08 2009" 
dataGiornata[13] = "november 22 2009" 
dataGiornata[14] = "november 29 2009" 
dataGiornata[15] = "december 06 2009" 
dataGiornata[16] = "december 13 2009" 
dataGiornata[17] = "december 20 2009" 
dataGiornata[18] = "january 06 2010" 
dataGiornata[19] = "january 10 2010" 
dataGiornata[20] = "january 17 2010" 
dataGiornata[21] = "january 24 2010" 
dataGiornata[22] = "january 31 2010" 
dataGiornata[23] = "february 07 2010" 
dataGiornata[24] = "february 14 2010" 
dataGiornata[25] = "february 21 2010" 
dataGiornata[26] = "february 28 2010" 
dataGiornata[27] = "march 07 2010" 
dataGiornata[28] = "march 14 2010" 
dataGiornata[29] = "march 21 2010" 
dataGiornata[30] = "march 24 2010" 
dataGiornata[31] = "march 28 2010" 
dataGiornata[32] = "april 04 2010" 
dataGiornata[33] = "april 11 2010" 
dataGiornata[34] = "april 18 2010" 
dataGiornata[35] = "april 25 2010" 
dataGiornata[36] = "may 02 2010" 
dataGiornata[37] = "may 09 2010" 
dataGiornata[38] = "may 16 2010" 

function initArray() {  
	this.length = initArray.arguments.length
    for (var i = 0; i < this.length; i++)
    this[i+1] = initArray.arguments[i]
}
var DOWArray = new initArray("Dom","Lun","Mar","Mer","Gio","Ven","Sab")
var MOYArray = new initArray("Gen","Feb","Mar","Apr","Mag","Giu","Lug","Ago","Set","Ott","Nov","Dic")
var Year
//for (t = 1; t < dataGiornata.length-1 ;t++ ) {
for (t = 1; t < dataGiornata.length ;t++ ) {
	data = new Date(dataGiornata[t])
	Year = data.getYear()
	if (Year < 2000)
		Year = Year + 1900
	dataGiornata[t] = DOWArray[(data.getDay()+1)] + " " + data.getDate() + " " + MOYArray[(data.getMonth()+1)] + " " + Year
}